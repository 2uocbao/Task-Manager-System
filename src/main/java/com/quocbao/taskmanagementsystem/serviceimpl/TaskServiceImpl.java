package com.quocbao.taskmanagementsystem.serviceimpl;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.common.ConvertData;
import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.PriorityEnum;
import com.quocbao.taskmanagementsystem.common.RoleEnum;
import com.quocbao.taskmanagementsystem.common.StatusEnum;
import com.quocbao.taskmanagementsystem.entity.Task;
import com.quocbao.taskmanagementsystem.entity.Team;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.events.Assign.AssignEvent;
import com.quocbao.taskmanagementsystem.events.Assign.HaveDeadlineEvent;
import com.quocbao.taskmanagementsystem.exception.AccessDeniedException;
import com.quocbao.taskmanagementsystem.exception.ForbiddenException;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.TaskRequest;
import com.quocbao.taskmanagementsystem.payload.response.TaskResponse;
import com.quocbao.taskmanagementsystem.payload.response.TaskStatusResponse;
import com.quocbao.taskmanagementsystem.repository.TaskRepository;
import com.quocbao.taskmanagementsystem.service.TaskService;
import com.quocbao.taskmanagementsystem.service.utils.AuthenticationService;
import com.quocbao.taskmanagementsystem.service.utils.TaskAssignmentHelperService;
import com.quocbao.taskmanagementsystem.service.utils.TeamHelperService;
import com.quocbao.taskmanagementsystem.service.utils.TeamMemberHelperService;
import com.quocbao.taskmanagementsystem.specifications.TaskSpecification;

@Service
public class TaskServiceImpl implements TaskService {

	private final ApplicationEventPublisher applicationEventPublisher;

	private final TaskRepository taskRepository;

	private final TeamHelperService teamHelperService;

	private final TeamMemberHelperService teamMemberHelperService;

	private final TaskAssignmentHelperService taskAssignmentHelperService;

	private final AuthenticationService authService;

	private final IdEncoder idEncoder;

	public TaskServiceImpl(ApplicationEventPublisher applicationEventPublisher, TaskRepository taskRepository,
			TeamHelperService teamHelperService, TaskAssignmentHelperService taskAssignmentHelperService,
			TeamMemberHelperService teamMemberHelperService, AuthenticationService authService,
			IdEncoder idEncoder) {
		this.applicationEventPublisher = applicationEventPublisher;
		this.taskRepository = taskRepository;
		this.teamHelperService = teamHelperService;
		this.teamMemberHelperService = teamMemberHelperService;
		this.taskAssignmentHelperService = taskAssignmentHelperService;
		this.authService = authService;
		this.idEncoder = idEncoder;
	}

	@Override
	public TaskResponse createTask(String teamId, TaskRequest taskRequest) {
		Long currentUserId = authService.getUserIdInContext();
		Long teamIdLong = idEncoder.decode(teamId);
		if (!teamHelperService.isLeaderOfTeam(currentUserId, teamIdLong)) {
			throw new ForbiddenException("User can not create new task in this team");
		}
		Task task = new Task(taskRequest);
		User user = User.builder().id(currentUserId).build();
		Team team = Team.builder().id(teamIdLong).build();
		task.setTeam(team);
		task.setUser(user);
		task = taskRepository.save(task);
		applicationEventPublisher.publishEvent(new AssignEvent(task.getId(), currentUserId));
		return new TaskResponse(task);
	}

	@Override
	public TaskResponse getTask(String taskId) {
		Long currentUserId = authService.getUserIdInContext();
		Long taskIdLong = idEncoder.decode(taskId);
		if (!taskAssignmentHelperService.isUserInTask(currentUserId, taskIdLong)) {
			throw new AccessDeniedException("User can not retrieve this task");
		}
		return taskRepository.findById(taskIdLong).map(task -> {
			TaskResponse taskResponse = new TaskResponse(task);
			taskResponse.setId(taskId);
			taskResponse.setUserId(idEncoder.encode(task.getUser().getId()));
			return taskResponse;
		}).orElseThrow(() -> new ResourceNotFoundException("Task not found"));
	}

	@Override
	public TaskResponse updateTask(String taskId, TaskRequest taskRequest) {
		Long currentUserId = authService.getUserIdInContext();
		Long taskIdLong = idEncoder.decode(taskId);
		if (!taskAssignmentHelperService.isRoleUserInTask(currentUserId, taskIdLong, RoleEnum.ADMIN)) {
			throw new ForbiddenException("User do not have permission");
		}
		return taskRepository.findById(taskIdLong).map(task -> {
			Task taskUpdate = task.updateTask(taskRequest);
			return new TaskResponse(taskRepository.save(taskUpdate));
		}).orElseThrow(() -> new ResourceNotFoundException("Task not found for update."));
	}

	@Override
	public void deleteTask(String taskId) {
		Long currentUserId = authService.getUserIdInContext();
		Long taskIdLong = idEncoder.decode(taskId);
		taskRepository.findById(taskIdLong).ifPresentOrElse(task -> {
			if (!taskAssignmentHelperService.isRoleUserInTask(currentUserId, taskIdLong, RoleEnum.ADMIN)) {
				throw new ForbiddenException("User do not have permission");
			}
			taskRepository.delete(task);
		}, () -> {
			throw new ResourceNotFoundException("Not found task need delete.");
		});
	}

	@Override
	public Page<TaskResponse> getTasks(String teamId, String status, String priority, String startDate, String endDate,
			Pageable pageable) {
		Long currentUserId = authService.getUserIdInContext();
		Long teamIdLong = idEncoder.decode(teamId);
		if (!teamMemberHelperService.isMemberTeam(currentUserId, teamIdLong)) {
			throw new AccessDeniedException("User can not access to list task of this team");
		}
		return taskRepository.getTask(currentUserId, teamIdLong, StatusEnum.valueOf(status),
				PriorityEnum.valueOf(priority), ConvertData.toTimestamp(startDate), ConvertData.toTimestamp(endDate),
				pageable).map(task -> {
					return new TaskResponse(idEncoder.encode(task.getId()), task.getTitle(), task.getPriority(),
							task.getDueAt(), task.getCommentCount(), task.getReportCount());
				});
	}

	@Override
	public TaskResponse updateStatus(String taskId, TaskRequest taskRequest) {
		Long currentUserId = authService.getUserIdInContext();
		Long taskIdLong = idEncoder.decode(taskId);
		if (!taskAssignmentHelperService.isUserInTask(currentUserId, taskIdLong)) {
			throw new AccessDeniedException("User can not update status of this task");
		}
		return taskRepository.findById(taskIdLong).map(task -> {
			task.setStatus(StatusEnum.valueOf(taskRequest.getStatus()));
			return new TaskResponse(taskRepository.save(task));
		}).orElseThrow(() -> new ResourceNotFoundException("Request failed"));
	}

	@Override
	public TaskResponse updatePriority(String taskId, TaskRequest taskRequest) {
		Long currentUserId = authService.getUserIdInContext();
		Long taskIdLong = idEncoder.decode(taskId);
		if (!taskAssignmentHelperService.isRoleUserInTask(currentUserId, taskIdLong, RoleEnum.ADMIN)) {
			throw new AccessDeniedException("User can not update priority of this task");
		}
		return taskRepository.findById(taskIdLong).map(task -> {
			task.setPriority(PriorityEnum.valueOf(taskRequest.getPriority()));
			return new TaskResponse(taskRepository.save(task));
		}).orElseThrow(() -> new ResourceNotFoundException("Request failed"));
	}

	@Override
	public Page<TaskResponse> searchTasks(String teamId, String keyword, Pageable pageable) {
		Long currentUserId = authService.getUserIdInContext();
		Long teamIdLong = idEncoder.decode(teamId);
		return taskRepository.searchTask(teamIdLong, currentUserId, keyword, pageable)
				.map(t -> new TaskResponse(idEncoder.encode(t.getId()), t.getTitle(), t.getStatus(), t.getDueAt(),
						t.getCommentCount(), t.getReportCount()));
	}

	@Override
	public List<TaskStatusResponse> getTaskSummaryInTeam(String teamId) {
		Long currentUserId = authService.getUserIdInContext();
		return taskRepository.getTaskSummary(currentUserId, idEncoder.decode(teamId)).stream().map(taskSummary -> {
			return new TaskStatusResponse(taskSummary.getStatus(), taskSummary.getTaskCount());
		}).toList();
	}

	@Override
	public void listSchudeledTasks(Timestamp startTime, Timestamp endTime) {
		List<Task> tasks = taskRepository
				.findAll(Specification.where(TaskSpecification.getTaskByDate(startTime, endTime)));
		tasks.stream().forEach(task -> {
			applicationEventPublisher.publishEvent(new HaveDeadlineEvent(task.getId()));
		});
	}
}
