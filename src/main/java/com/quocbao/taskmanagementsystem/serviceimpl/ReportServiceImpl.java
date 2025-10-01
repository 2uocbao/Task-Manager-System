package com.quocbao.taskmanagementsystem.serviceimpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.quocbao.taskmanagementsystem.common.ConvertData;
import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.StorageProperties;
import com.quocbao.taskmanagementsystem.common.TypeEnum;
import com.quocbao.taskmanagementsystem.entity.Report;
import com.quocbao.taskmanagementsystem.entity.Task;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.events.Assign.HaveReportEvent;
import com.quocbao.taskmanagementsystem.exception.AccessDeniedException;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.ReportRequest;
import com.quocbao.taskmanagementsystem.payload.response.ReportResponse;
import com.quocbao.taskmanagementsystem.repository.ReportRepository;
import com.quocbao.taskmanagementsystem.service.ReportService;
import com.quocbao.taskmanagementsystem.service.utils.AuthenticationService;
import com.quocbao.taskmanagementsystem.service.utils.TaskAssignmentHelperService;
import com.quocbao.taskmanagementsystem.service.utils.TaskHelperService;
import com.quocbao.taskmanagementsystem.specifications.ReportSpecification;

@Service
public class ReportServiceImpl implements ReportService {

	static final Logger LOGGER = LoggerFactory.getLogger(ReportServiceImpl.class);

	private final ReportRepository reportRepository;

	private final TaskHelperService taskHelperService;

	private final TaskAssignmentHelperService taskAssignmentHelperService;

	private final AuthenticationService authService;

	private final IdEncoder idEncoder;

	private final StorageProperties storageProperties;

	private final ApplicationEventPublisher applicationEventPublisher;

	public ReportServiceImpl(ReportRepository reportRepository, TaskHelperService taskHelperService,
			TaskAssignmentHelperService taskAssignmentHelperService,
			AuthenticationService authService, IdEncoder idEncoder, StorageProperties storageProperties,
			ApplicationEventPublisher applicationEventPublisher) {
		this.reportRepository = reportRepository;
		this.taskHelperService = taskHelperService;
		this.taskAssignmentHelperService = taskAssignmentHelperService;
		this.authService = authService;
		this.idEncoder = idEncoder;
		this.storageProperties = storageProperties;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	TypeEnum typeEnum = TypeEnum.FILE;

	@Override
	public ReportResponse createReportForFile(MultipartFile file, String taskId) throws IOException {
		Long currentUserId = authService.getUserIdInContext();
		Long taskIdLong = idEncoder.decode(taskId);
		if (!isMemberInTask(taskIdLong, currentUserId)) {
			throw new AccessDeniedException("User do not have access");
		}
		if (!taskHelperService.isTaskExist(taskIdLong)) {
			throw new ResourceNotFoundException("Task not found");
		}

		Path filePath = Path.of(storageProperties.getLocation(), taskId + file.getOriginalFilename());

		file.transferTo(filePath.toFile());

		if (Boolean.TRUE.equals(ConvertData.isImage(file.getOriginalFilename()))) {
			typeEnum = TypeEnum.PHOTO;
		}

		User user = User.builder().id(currentUserId).build();
		Task task = Task.builder().id(taskIdLong).build();
		Report report = Report.builder().user(user).task(task).fileName(file.getOriginalFilename())
				.filePath(filePath.toString()).type(typeEnum).build();

		Report reportResult = reportRepository.save(report);

		sendNotificationToMember(taskIdLong, currentUserId);
		return new ReportResponse(reportResult);
	}

	@Override
	public ReportResponse createReportForLink(ReportRequest reportRequest) {
		Long currentUserId = authService.getUserIdInContext();
		Long taskId = idEncoder.decode(reportRequest.getTaskId());
		if (!isMemberInTask(taskId, currentUserId)) {
			throw new AccessDeniedException("User do not have access");
		}
		if (!taskHelperService.isTaskExist(taskId)) {
			throw new ResourceNotFoundException("Task not found");
		}
		Report report = Report.builder().user(User.builder().id(currentUserId).build())
				.task(Task.builder().id(taskId).build())
				.externalUrl(reportRequest.getExternalUrl()).type(TypeEnum.URL).build();
		Report reportResult = reportRepository.save(report);
		sendNotificationToMember(taskId, currentUserId);
		return new ReportResponse(reportResult);
	}

	@Override
	public void deleteReport(String reportId) {
		Long currentUserId = authService.getUserIdInContext();
		Long reportIdLong = idEncoder.decode(reportId);

		reportRepository.findById(reportIdLong).ifPresentOrElse(report -> {
			if (!taskHelperService.isCreatorTask(currentUserId, report.getTask().getId())) {
				throw new AccessDeniedException("User do not have permission");
			}
			if (report.getType().equals(TypeEnum.PHOTO) || report.getType().equals(TypeEnum.FILE)) {
				String taskIdString = idEncoder.encode(report.getTask().getId());
				try {
					Path filePath = Path.of(storageProperties.getLocation(), taskIdString + report.getFileName());
					Files.deleteIfExists(filePath);
					LOGGER.info("Delete file: " + report.getFileName());
				} catch (IOException e) {
					LOGGER.error("Error deleting file: " + e.getMessage());
				}
			}
			reportRepository.delete(report);
		}, () -> {
			throw new ResourceNotFoundException("Report not found");
		});

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
		Long reportIdLong = idEncoder.decode(reportId);
		Long taskIdLong = idEncoder.decode(taskId);
		if (!isMemberInTask(taskIdLong, currentUserId)) {
			throw new AccessDeniedException("User do not have access");
		}
		return reportRepository.findById(reportIdLong).map(report -> {
			return Path.of(storageProperties.getLocation()).resolve(taskId + report.getFileName());
		}).orElseThrow(() -> new ResourceNotFoundException("File not found"));
	}

	protected Boolean isMemberInTask(Long taskId, Long userId) {
		return taskAssignmentHelperService.isUserInTask(userId, taskId);
	}

	protected void sendNotificationToMember(Long taskId, Long userId) {
		applicationEventPublisher.publishEvent(new HaveReportEvent(userId, taskId));
	}
}
