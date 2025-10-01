package com.quocbao.taskmanagementsystem.serviceimpl;

import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.common.ConvertData;
import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.NotificationType;
import com.quocbao.taskmanagementsystem.common.RoleEnum;
import com.quocbao.taskmanagementsystem.entity.Task;
import com.quocbao.taskmanagementsystem.entity.TaskAssignment;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.events.Notification.NotificationAddEvent;
import com.quocbao.taskmanagementsystem.exception.AccessDeniedException;
import com.quocbao.taskmanagementsystem.exception.DuplicateException;
import com.quocbao.taskmanagementsystem.exception.ForbiddenException;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.TaskAssignRequest;
import com.quocbao.taskmanagementsystem.payload.response.TaskAssignResponse;
import com.quocbao.taskmanagementsystem.repository.TaskAssignmentRepository;
import com.quocbao.taskmanagementsystem.service.TaskAssignService;
import com.quocbao.taskmanagementsystem.service.utils.AuthenticationService;
import com.quocbao.taskmanagementsystem.service.utils.TaskHelperService;
import com.quocbao.taskmanagementsystem.service.utils.TeamMemberHelperService;
import com.quocbao.taskmanagementsystem.specifications.TaskAssignSpecification;

@Service
public class TaskAssignServiceImpl implements TaskAssignService {

	private final ApplicationEventPublisher applicationEventPublisher;

	private final TaskAssignmentRepository taskAssignmentRepository;

	private final TeamMemberHelperService teamMemberHelperService;

	private final TaskHelperService taskHelperService;

	private final AuthenticationService authService;

	private final IdEncoder idEncoder;

	public TaskAssignServiceImpl(ApplicationEventPublisher applicationEventPublisher,
			TaskAssignmentRepository taskAssignmentRepository,
			TeamMemberHelperService teamMemberHelperService, TaskHelperService taskHelperService,
			AuthenticationService authService, IdEncoder idEncoder) {
		this.applicationEventPublisher = applicationEventPublisher;
		this.taskAssignmentRepository = taskAssignmentRepository;
		this.teamMemberHelperService = teamMemberHelperService;
		this.taskHelperService = taskHelperService;
		this.authService = authService;
		this.idEncoder = idEncoder;
	}

	@Override
	public TaskAssignResponse addAssign(String taskId, TaskAssignRequest taskAssignRequest) {
		Long currentUserId = authService.getUserIdInContext();
		Long taskIdLong = idEncoder.decode(taskId);
		Long assignerId = idEncoder.decode(taskAssignRequest.getToUserId());
		// User is creator task
		if (!isAdmin(taskIdLong, currentUserId)) {
			throw new AccessDeniedException("User do not have permission");
		}

		// Assignee is already exist in task

		if (isAlreadyExist(taskIdLong, assignerId)) {
			throw new DuplicateException("User already exist in task");
		}

		// Get task
		Task task = taskHelperService.getTask(taskIdLong).orElseThrow(() -> {
			throw new ResourceNotFoundException("Task can not find");
		});

		// Assignee have in the team have this task
		if (!teamMemberHelperService.isMemberTeam(assignerId, task.getTeam().getId())) {
			throw new ForbiddenException("Assignee did not have in team have this task");
		}

		User user = User.builder().id(assignerId).build();

		TaskAssignment taskAssignment = taskAssignmentRepository.save(TaskAssignment.builder().task(task)
				.user(User.builder().id(assignerId).build())
				.role(RoleEnum.MEMBER).build());

		sendNotificationAssign(currentUserId, user.getId(), taskIdLong,
				NotificationType.NEW_ASSIGN.toString());

		return new TaskAssignResponse(idEncoder.encode(taskAssignment.getId()), taskAssignRequest.getToUserId(),
				user.getFirstName() + " " + user.getLastName(), user.getMention(), user.getImage(),
				RoleEnum.MEMBER.toString(),
				ConvertData.timeStampToString(taskAssignment.getJoinedAt()));
	}

	@Override
	public void removeAssign(String taskId, String assignId, TaskAssignRequest taskAssignRequest) {
		Long currentUserId = authService.getUserIdInContext();
		Long assignIdLong = idEncoder.decode(assignId);
		Long taskIdLong = idEncoder.decode(taskId);
		if (!isAdmin(taskIdLong, currentUserId)) {
			throw new AccessDeniedException("User do not have permission");
		}
		taskAssignmentRepository.findById(assignIdLong).ifPresent(taskAssign -> {
			if (taskAssign.getUser().getId() == currentUserId) {
				throw new ForbiddenException("Can not do this");
			}
			taskAssignmentRepository.delete(taskAssign);
			sendNotificationAssign(currentUserId, taskAssign.getUser().getId(), taskIdLong,
					NotificationType.REMOVE_ASSIGN.toString());
		});
	}

	@Override
	public List<TaskAssignResponse> getTaskAssigns(String taskId) {
		Long taskIdL = idEncoder.decode(taskId);
		return taskAssignmentRepository.getTaskAssignments(taskIdL).stream()
				.map(taskAssignment -> {
					String username = Optional.ofNullable(taskAssignment.getFirstName()).orElse("") + " "
							+ Optional.ofNullable(taskAssignment.getLastName()).orElse("");
					String image = Optional.ofNullable(taskAssignment.getImage()).orElse("");
					return new TaskAssignResponse(idEncoder.encode(taskAssignment.getId()),
							idEncoder.encode(taskAssignment.getAssignerId()), username, taskAssignment.getMention(),
							image,
							taskAssignment.getRole(),
							ConvertData.timeStampToString(taskAssignment.getJoinedAt()));
				}).toList();
	}

	@Override
	public void assigneeHaveDeadline(Long taskId) {
		List<TaskAssignment> taskAssignments = taskAssignmentRepository
				.findAll(Specification.where(TaskAssignSpecification.getByTask(taskId)));
		if (taskAssignments.size() == 1) {
			sendNotificationAssign(taskId, taskAssignments.getFirst().getUser().getId(), taskId,
					NotificationType.DUEAT.toString());
		} else {
			taskAssignments.stream().forEach(assignee -> {
				if (!assignee.getRole().equals(RoleEnum.ADMIN))
					sendNotificationAssign(taskId, assignee.getUser().getId(), taskId,
							NotificationType.DUEAT.toString());
			});
		}

	}

	@Override
	public void createAdminTask(Long userId, Long taskId) {
		TaskAssignment taskAssignment = TaskAssignment.builder().user(User.builder().id(userId).build())
				.task(Task.builder().id(taskId).build()).role(RoleEnum.ADMIN).build();

		taskAssignmentRepository.save(taskAssignment);
	}

	protected Specification customSpecification(Long userId, Long taskId) {
		return Specification.where(TaskAssignSpecification.getByTask(taskId))
				.and(TaskAssignSpecification.getByUserAssign(userId));
	}

	protected Boolean isAlreadyExist(Long taskId, Long userId) {
		return taskAssignmentRepository.exists(customSpecification(userId, taskId));
	}

	protected Boolean isAdmin(Long taskId, Long userId) {
		return taskAssignmentRepository.exists(Specification
				.where(customSpecification(userId, taskId)
						.and(TaskAssignSpecification.getByRole(RoleEnum.ADMIN))));
	}

	protected void sendNotificationAssign(Long senderId, Long receiverId, Long taskId, String contentType) {
		applicationEventPublisher
				.publishEvent(new NotificationAddEvent(senderId, receiverId, taskId, NotificationType.TASK.toString(),
						contentType));
	}
}
