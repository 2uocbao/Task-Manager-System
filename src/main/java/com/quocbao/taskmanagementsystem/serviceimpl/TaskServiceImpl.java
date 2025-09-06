package com.quocbao.taskmanagementsystem.serviceimpl;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quocbao.taskmanagementsystem.common.ConvertData;
import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.MethodGeneral;
import com.quocbao.taskmanagementsystem.common.NotificationType;
import com.quocbao.taskmanagementsystem.common.PriorityEnum;
import com.quocbao.taskmanagementsystem.common.StatusEnum;
import com.quocbao.taskmanagementsystem.entity.Task;
import com.quocbao.taskmanagementsystem.entity.Team;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.events.DeleteEvent.NotifiDeletedEvent;
import com.quocbao.taskmanagementsystem.events.DeleteEvent.ReportDeletedEvent;
import com.quocbao.taskmanagementsystem.exception.DuplicateException;
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
@Transactional
public class TaskServiceImpl implements TaskService {

	private final ApplicationEventPublisher applicationEventPublisher;

	private final TaskRepository taskRepository;

	private final TeamHelperService teamHelperService;

	private final TeamMemberHelperService teamMemberHelperService;

	private final TaskAssignmentHelperService taskAssignmentHelperService;

	private final AuthenticationService authService;

	private final MethodGeneral methodGeneral;

	private final IdEncoder idEncoder;

	public TaskServiceImpl(ApplicationEventPublisher applicationEventPublisher, TaskRepository taskRepository,
			TeamHelperService teamHelperService, TaskAssignmentHelperService taskAssignmentHelperService,
			TeamMemberHelperService teamMemberHelperService, AuthenticationService authService,
			MethodGeneral methodGeneral, IdEncoder idEncoder) {
		this.applicationEventPublisher = applicationEventPublisher;
		this.taskRepository = taskRepository;
		this.teamHelperService = teamHelperService;
		this.teamMemberHelperService = teamMemberHelperService;
		this.taskAssignmentHelperService = taskAssignmentHelperService;
		this.authService = authService;
		this.methodGeneral = methodGeneral;
		this.idEncoder = idEncoder;
	}

	@Override
	public TaskResponse createTask(String teamId, TaskRequest taskRequest) {
		try {
			Long currentUserId = authService.getUserIdInContext();
			if (!teamHelperService.isLeaderOfTeam(currentUserId, teamId)) {
				throw new ForbiddenException("User can not create new task in this team");
			}
			Task task = new Task(taskRequest);
			User user = User.builder().id(currentUserId).build();
			Team team = Team.builder().id(idEncoder.decode(teamId)).build();
			task.setTeam(team);
			task.setUser(user);
			task = taskRepository.save(task);
			taskAssignmentHelperService.addTeamMember(currentUserId, task.getId());
			return new TaskResponse(task);
		} catch (Exception e) {
			throw new ResourceNotFoundException();
		}
	}

	@Override
	public TaskResponse getTask(String taskId) {
		Long currentUserId = authService.getUserIdInContext();
		if (!taskAssignmentHelperService.isUserInTask(currentUserId, taskId)) {
			isCreatorTask(currentUserId, taskId);
		}
		return taskRepository.findById(idEncoder.decode(taskId)).map(task -> {
			TaskResponse taskResponse = new TaskResponse(task);
			taskResponse.setId(idEncoder.encode(task.getId()));
			taskResponse.setUserId(idEncoder.encode(task.getUser().getId()));
			return taskResponse;
		}).orElseThrow(() -> new ResourceNotFoundException("Task not found"));
	}

	@Override
	public TaskResponse updateTask(String taskId, TaskRequest taskRequest) {
		Long currentUserId = authService.getUserIdInContext();
		isCreatorTask(currentUserId, taskId);
		return taskRepository.findById(idEncoder.decode(taskId)).map(task -> {
			task = task.updateTask(taskRequest);
			return new TaskResponse(taskRepository.save(task));
		}).orElseThrow(() -> new ResourceNotFoundException("Task not found for update."));
	}

	@Override
	@Transactional
	public void deleteTask(String taskId) {
		Long currentUserId = authService.getUserIdInContext();
		Long taskIdLong = idEncoder.decode(taskId);
		taskRepository.findById(taskIdLong).ifPresentOrElse(t -> {
			isCreatorTask(currentUserId, taskId);
			if (taskAssignmentHelperService.haveAssign(taskId))
				throw new DuplicateException("Remove the member from this task before deleting it.");
			applicationEventPublisher
					.publishEvent(new NotifiDeletedEvent(taskIdLong, NotificationType.TASK.toString()));
			applicationEventPublisher.publishEvent(new ReportDeletedEvent(taskIdLong));
			taskRepository.delete(t);

		}, () -> new ResourceNotFoundException("Not found task need delete."));
	}

	@Override
	public Page<TaskResponse> getTasks(String teamId, String status, String priority, String startDate, String endDate,
			Pageable pageable) {
		Long currentUserId = authService.getUserIdInContext();
		boolean isMember = teamMemberHelperService.isMemberTeam(currentUserId, teamId);
		if (!teamHelperService.isLeaderOfTeam(currentUserId, teamId)) {
			if (!isMember) {
				throw new ForbiddenException("User does not have access");
			}
		}
		if (isMember) {
			return listTaskByMember(currentUserId, teamId, status, priority, startDate, endDate, pageable);
		}
		return listTasksByLeader(currentUserId, teamId, status, priority, startDate, endDate, pageable);
	}

	@Override
	public TaskResponse updateStatus(String taskId, TaskRequest taskRequest) {
		Long currentUserId = authService.getUserIdInContext();
		return taskRepository.findById(idEncoder.decode(taskId)).map(task -> {
			if (!taskAssignmentHelperService.isUserInTask(currentUserId, taskId)) {
				methodGeneral.validatePermission(currentUserId, task.getUser().getId());
			}
			task.setStatus(StatusEnum.valueOf(taskRequest.getStatus()));
			taskRepository.save(task);
			return new TaskResponse(taskRepository.save(task));
		}).orElseThrow(() -> new ResourceNotFoundException("Request failed"));
	}

	@Override
	public TaskResponse updatePriority(String taskId, TaskRequest taskRequest) {
		Long currentUserId = authService.getUserIdInContext();
		return taskRepository.findById(idEncoder.decode(taskId)).map(task -> {
			methodGeneral.validatePermission(currentUserId, task.getUser().getId());
			task.setPriority(PriorityEnum.valueOf(taskRequest.getPriority()));
			taskRepository.save(task);
			return new TaskResponse(taskRepository.save(task));
		}).orElseThrow(() -> new ResourceNotFoundException("Request failed"));
	}

	@Override
	public Page<TaskResponse> searchTasks(String keyword, Pageable pageable) {
		Long currentUserId = authService.getUserIdInContext();
		return taskRepository.searchTask(currentUserId, keyword, pageable)
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

	protected void isCreatorTask(Long userId, String taskId) {
		Specification<Task> specification = Specification.where(
				TaskSpecification.getTaskById(idEncoder.decode(taskId)).and(TaskSpecification.getTaskByUserId(userId)));
		if (!taskRepository.exists(specification)) {
			throw new ForbiddenException("User do not have permission");
		}
	}

	private Page<TaskResponse> listTaskByMember(Long userId, String teamId, String status, String priority,
			String startDate, String endDate, Pageable pageable) {
		return taskRepository.getTaskByMember(userId, idEncoder.decode(teamId), StatusEnum.valueOf(status),
				PriorityEnum.valueOf(priority), ConvertData.toTimestamp(startDate), ConvertData.toTimestamp(endDate),
				pageable).map(task -> {
					return new TaskResponse(idEncoder.encode(task.getId()), task.getTitle(), task.getPriority(),
							task.getDueAt(), task.getCommentCount(), task.getReportCount());
				});
	}

	private Page<TaskResponse> listTasksByLeader(Long userId, String teamId, String status, String priority,
			String startDate, String endDate, Pageable pageable) {
		return taskRepository.getTaskByLeader(userId, idEncoder.decode(teamId), StatusEnum.valueOf(status),
				PriorityEnum.valueOf(priority), ConvertData.toTimestamp(startDate), ConvertData.toTimestamp(endDate),
				pageable).map(task -> {
					return new TaskResponse(idEncoder.encode(task.getId()), task.getTitle(), task.getPriority(),
							task.getDueAt(), task.getCommentCount(), task.getReportCount());
				});
	}
}
