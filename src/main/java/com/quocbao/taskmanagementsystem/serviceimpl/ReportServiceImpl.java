package com.quocbao.taskmanagementsystem.serviceimpl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.quocbao.taskmanagementsystem.common.ConvertData;
import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.MethodGeneral;
import com.quocbao.taskmanagementsystem.common.StorageProperties;
import com.quocbao.taskmanagementsystem.common.TypeEnum;
import com.quocbao.taskmanagementsystem.entity.Report;
import com.quocbao.taskmanagementsystem.entity.Task;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.exception.UnauthorizedException;
import com.quocbao.taskmanagementsystem.payload.request.ReportRequest;
import com.quocbao.taskmanagementsystem.payload.response.ReportResponse;
import com.quocbao.taskmanagementsystem.repository.ReportRepository;
import com.quocbao.taskmanagementsystem.repository.TaskRepository;
import com.quocbao.taskmanagementsystem.service.ReportService;
import com.quocbao.taskmanagementsystem.specifications.ReportSpecification;

@Service
public class ReportServiceImpl implements ReportService {

	private final ReportRepository reportRepository;
	private final TaskRepository taskRepository;
	private final MethodGeneral methodGeneral;
	private final IdEncoder idEncoder;
	private final StorageProperties storageProperties;

	public ReportServiceImpl(ReportRepository reportRepository, TaskRepository taskRepository,
			MethodGeneral methodGeneral, IdEncoder idEncoder, StorageProperties storageProperties) {
		this.reportRepository = reportRepository;
		this.taskRepository = taskRepository;
		this.methodGeneral = methodGeneral;
		this.idEncoder = idEncoder;
		this.storageProperties = storageProperties;
	}

	TypeEnum typeEnum = TypeEnum.FILE;

	@Override
	public ReportResponse createReportForFile(MultipartFile file, String userId, String taskId) throws IOException {

		Task task = getTask(idEncoder.decode(taskId));

		if (task.getAssignTo() != null) {
			methodGeneral.havePermission(idEncoder.decode(userId), task.getUser().getId(), task.getAssignTo().getId());
		} else {

			methodGeneral.validatePermission(idEncoder.decode(userId), task.getUser().getId());
		}
		Path filePath = Paths.get(storageProperties.getLocation(), taskId + file.getOriginalFilename());

		file.transferTo(filePath.toFile());

		if (Boolean.TRUE.equals(ConvertData.isImage(file.getName()))) {
			typeEnum = TypeEnum.PHOTO;
		}

		User user = User.builder().id(idEncoder.decode(userId)).build();
		Report report = Report.builder().user(user).task(task).fileName(file.getOriginalFilename())
				.filePath(filePath.toString()).type(typeEnum).build();

		Report reportResult = reportRepository.save(report);
		return new ReportResponse(reportResult);

	}

	@Override
	public ReportResponse createReportForLink(ReportRequest reportRequest) {
		Task task = getTask(idEncoder.decode(reportRequest.getTaskId()));
		methodGeneral.havePermission(idEncoder.decode(reportRequest.getUserId()), task.getUser().getId(),
				task.getAssignTo().getId());
		Report report = Report.builder().user(User.builder().id(idEncoder.decode(reportRequest.getUserId())).build())
				.task(task).externalUrl(reportRequest.getExternalUrl()).type(TypeEnum.URL).build();

		Report reportResult = reportRepository.save(report);

		return new ReportResponse(reportResult);

	}

	@Override
	public void deleteReport(String reportId, String taskId, String userId) {
		Task task = getTask(idEncoder.decode(taskId));
		Report report = reportRepository.findById(idEncoder.decode(reportId))
				.orElseThrow(() -> new ResourceNotFoundException("Report not found"));
		if (task.getAssignTo() != null) {
			methodGeneral.havePermission(idEncoder.decode(userId), task.getUser().getId(), task.getAssignTo().getId());
		} else {

			methodGeneral.validatePermission(idEncoder.decode(userId), task.getUser().getId());
		}
		reportRepository.delete(report);
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
	public Path getFile(String userId, String taskId, String reportId) {
		Task task = getTask(idEncoder.decode(taskId));
		if (task.getAssignTo() != null) {
			methodGeneral.havePermission(idEncoder.decode(userId), task.getUser().getId(), task.getAssignTo().getId());
		} else {

			methodGeneral.validatePermission(idEncoder.decode(userId), task.getUser().getId());
		}
		Report report = reportRepository.findById(idEncoder.decode(reportId))
				.orElseThrow(() -> new ResourceNotFoundException("File not found"));
		if (report.getType().equals(TypeEnum.URL)) {
			throw new UnauthorizedException("Request do not except");
		}
		return Paths.get(storageProperties.getLocation()).resolve(taskId + report.getFileName());

	}

	private Task getTask(long taskId) {
		return taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task not found"));
	}

}
