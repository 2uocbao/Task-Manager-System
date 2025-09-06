package com.quocbao.taskmanagementsystem.serviceimpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.quocbao.taskmanagementsystem.common.ConvertData;
import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.MethodGeneral;
import com.quocbao.taskmanagementsystem.common.NotificationType;
import com.quocbao.taskmanagementsystem.common.StorageProperties;
import com.quocbao.taskmanagementsystem.common.TypeEnum;
import com.quocbao.taskmanagementsystem.entity.Report;
import com.quocbao.taskmanagementsystem.entity.Task;
import com.quocbao.taskmanagementsystem.entity.TaskAssignment;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.events.DeleteEvent.ReportDeletedEvent;
import com.quocbao.taskmanagementsystem.events.NotifiEvent.ReportEvent;
import com.quocbao.taskmanagementsystem.exception.ForbiddenException;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.ReportRequest;
import com.quocbao.taskmanagementsystem.payload.response.ReportResponse;
import com.quocbao.taskmanagementsystem.repository.ReportRepository;
import com.quocbao.taskmanagementsystem.service.ReportService;
import com.quocbao.taskmanagementsystem.service.utils.AuthenticationService;
import com.quocbao.taskmanagementsystem.service.utils.TaskAssignmentHelperService;
import com.quocbao.taskmanagementsystem.service.utils.TaskHelperService;
import com.quocbao.taskmanagementsystem.service.utils.UserHelperService;
import com.quocbao.taskmanagementsystem.specifications.ReportSpecification;

@Service
public class ReportServiceImpl implements ReportService {

	static final Logger LOGGER = LoggerFactory.getLogger(ReportServiceImpl.class);

	private final ReportRepository reportRepository;

	private final TaskHelperService taskHelperService;

	private final TaskAssignmentHelperService taskAssignmentHelperService;

	private final UserHelperService userHelperService;

	private final AuthenticationService authService;

	private final IdEncoder idEncoder;

	private final StorageProperties storageProperties;

	private final ApplicationEventPublisher applicationEventPublisher;

	private final MethodGeneral methodGeneral;

	public ReportServiceImpl(ReportRepository reportRepository, TaskHelperService taskHelperService,
			TaskAssignmentHelperService taskAssignmentHelperService, UserHelperService userHelperService,
			AuthenticationService authService, IdEncoder idEncoder, StorageProperties storageProperties,
			ApplicationEventPublisher applicationEventPublisher, MethodGeneral methodGeneral) {
		this.reportRepository = reportRepository;
		this.taskHelperService = taskHelperService;
		this.taskAssignmentHelperService = taskAssignmentHelperService;
		this.userHelperService = userHelperService;
		this.authService = authService;
		this.idEncoder = idEncoder;
		this.storageProperties = storageProperties;
		this.applicationEventPublisher = applicationEventPublisher;
		this.methodGeneral = methodGeneral;
	}

	TypeEnum typeEnum = TypeEnum.FILE;

	@Override
	public ReportResponse createReportForFile(MultipartFile file, String taskId) throws IOException {
		Long currentUserId = authService.getUserIdInContext();
		Boolean hasAccess = isMemberInTask(taskId, currentUserId) || isLeaderInTask(taskId, currentUserId);
		if (!hasAccess) {
			throw new ForbiddenException("User do not have access");
		}
		if (!taskHelperService.isTaskExist(taskId)) {
			throw new ResourceNotFoundException("Task not found");
		}

		Path filePath = Path.of(storageProperties.getLocation(), taskId + file.getOriginalFilename());

		file.transferTo(filePath.toFile());

		LOGGER.info(file.getOriginalFilename());
		if (Boolean.TRUE.equals(ConvertData.isImage(file.getOriginalFilename()))) {
			typeEnum = TypeEnum.PHOTO;
		}

		User user = User.builder().id(currentUserId).build();
		Task task = Task.builder().id(idEncoder.decode(taskId)).build();
		Report report = Report.builder().user(user).task(task).fileName(file.getOriginalFilename())
				.filePath(filePath.toString()).type(typeEnum).build();

		Report reportResult = reportRepository.save(report);

		sendNotificationToMember(taskId, currentUserId);
		return new ReportResponse(reportResult);
	}

	@Override
	public ReportResponse createReportForLink(ReportRequest reportRequest) {
		Long currentUserId = authService.getUserIdInContext();
		Boolean hasAccess = isMemberInTask(reportRequest.getTaskId(), currentUserId)
				|| isLeaderInTask(reportRequest.getTaskId(), currentUserId);
		if (!hasAccess) {
			throw new ForbiddenException("User do not have access");
		}
		if (!taskHelperService.isTaskExist(reportRequest.getTaskId())) {
			throw new ResourceNotFoundException("Task not found");
		}
		Report report = Report.builder().user(User.builder().id(currentUserId).build())
				.task(Task.builder().id(idEncoder.decode(reportRequest.getTaskId())).build())
				.externalUrl(reportRequest.getExternalUrl()).type(TypeEnum.URL).build();
		Report reportResult = reportRepository.save(report);
		sendNotificationToMember(reportRequest.getTaskId(), currentUserId);
		return new ReportResponse(reportResult);
	}

	@Override
	public void deleteReport(String reportId, String taskId) {
		Long currentUserId = authService.getUserIdInContext();
		reportRepository.findById(idEncoder.decode(reportId)).ifPresentOrElse(report -> {
			if (!isLeaderInTask(taskId, currentUserId)) {
				methodGeneral.validatePermission(currentUserId, report.getUser().getId());
			}
			if (report.getType().equals(TypeEnum.PHOTO) || report.getType().equals(TypeEnum.FILE)) {
				try {
					Path filePath = Path.of(storageProperties.getLocation(), taskId + report.getFileName());
					Files.deleteIfExists(filePath);
					LOGGER.info("Delete file: " + report.getFileName());
				} catch (IOException e) {
					LOGGER.error("Error deleting file: " + e.getMessage());
				}
			}
			reportRepository.delete(report);
		}, () -> new ResourceNotFoundException("Report not found"));

	}

	@Override
	public List<ReportResponse> getReports(String taskId) {
		Specification<Report> specification = Specification
				.where(ReportSpecification.getReportByTaskId(idEncoder.decode(taskId)));
		List<Report> reportPage = reportRepository.findAll(specification,
				Sort.by(Direction.fromString(Direction.DESC.toString()), "createdAt"));
		return reportPage.stream().map(ReportResponse::new).toList();
	}

	@Override
	public Path getFile(String taskId, String reportId) {
		Long currentUserId = authService.getUserIdInContext();
		Boolean hasAccess = isMemberInTask(taskId, currentUserId) || isLeaderInTask(taskId, currentUserId);
		if (!hasAccess) {
			throw new ForbiddenException("User do not have access");
		}
		return reportRepository.findById(idEncoder.decode(reportId)).map(report -> {
			return Path.of(storageProperties.getLocation()).resolve(taskId + report.getFileName());
		}).orElseThrow(() -> new ResourceNotFoundException("File not found"));
	}

	protected Boolean isMemberInTask(String taskId, Long userId) {
		return taskAssignmentHelperService.isUserInTask(userId, taskId);
	}

	protected Boolean isLeaderInTask(String taskId, Long userId) {
		return taskHelperService.isCreatorTask(userId, taskId);
	}

	protected void sendNotificationToMember(String taskId, Long userId) {
		User userReported = userHelperService.getUser(userId).get();
		Task task = taskHelperService.getTask(taskId).get();
		String userName = userReported.getFirstName() + " " + userReported.getLastName();
		String taskTitle = task.getTitle();
		// send notification to leader
		if (userId != task.getUser().getId()) {
			applicationEventPublisher.publishEvent(new ReportEvent(task.getId(), userReported.getId(),
					task.getUser().getId(), userName, taskTitle, NotificationType.REPORT.toString(),
					task.getUser().getToken() == null ? null : task.getUser().getToken(),
					task.getUser().getLanguage()));
		}
		// Fetch member in task
		List<TaskAssignment> taskAssignments = taskAssignmentHelperService.getAssignmentsByTaskId(taskId);
		taskAssignments.stream().forEach(taskAssign -> {
			// send notification
			if (taskAssign.getUser().getId() != userReported.getId()) {
				applicationEventPublisher.publishEvent(new ReportEvent(task.getId(), userReported.getId(),
						taskAssign.getUser().getId(), userName, taskTitle, NotificationType.REPORT.toString(),
						taskAssign.getUser().getToken() == null ? null : taskAssign.getUser().getToken(),
						taskAssign.getUser().getLanguage()));
			}
		});
	}

	@EventListener
	public void deleteReport(ReportDeletedEvent event) {
		String currentTaskId = idEncoder.encode(event.getTaskId());
		List<Report> reports = reportRepository.findAll(ReportSpecification.getReportByTaskId(event.getTaskId()));
		reports.stream().forEach(report -> {
			if (report.getType().equals(TypeEnum.PHOTO) || report.getType().equals(TypeEnum.FILE)) {
				try {
					Path filePath = Path.of(storageProperties.getLocation(), currentTaskId + report.getFileName());
					Files.deleteIfExists(filePath);
					LOGGER.info("Delete file: " + report.getFileName());
				} catch (IOException e) {
					LOGGER.error("Error deleting file: " + e.getMessage());
				}
			}
			reportRepository.delete(report);
		});
	}
}
