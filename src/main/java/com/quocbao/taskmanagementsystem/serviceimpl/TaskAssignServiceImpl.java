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
import com.quocbao.taskmanagementsystem.events.NotifiEvent.TaskEvent;
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
import com.quocbao.taskmanagementsystem.service.utils.UserHelperService;
import com.quocbao.taskmanagementsystem.specifications.TaskAssignSpecification;

@Service
public class TaskAssignServiceImpl implements TaskAssignService {

	private final ApplicationEventPublisher applicationEventPublisher;

	private final TaskAssignmentRepository taskAssignmentRepository;

	private final UserHelperService userHelperService;

	private final TeamMemberHelperService teamMemberHelperService;

	private final TaskHelperService taskHelperService;

	private final AuthenticationService authService;

	private final IdEncoder idEncoder;

	public TaskAssignServiceImpl(ApplicationEventPublisher applicationEventPublisher,
			TaskAssignmentRepository taskAssignmentRepository, UserHelperService userHelperService,
			TeamMemberHelperService teamMemberHelperService, TaskHelperService taskHelperService,
			AuthenticationService authService, IdEncoder idEncoder) {
		this.applicationEventPublisher = applicationEventPublisher;
		this.taskAssignmentRepository = taskAssignmentRepository;
		this.userHelperService = userHelperService;
		this.teamMemberHelperService = teamMemberHelperService;
		this.taskHelperService = taskHelperService;
		this.authService = authService;
		this.idEncoder = idEncoder;
	}

	@Override
	public TaskAssignResponse addAssign(String taskId, TaskAssignRequest taskAssignRequest) {
		Long currentUserId = authService.getUserIdInContext();
		// User is creator task
		if (!taskHelperService.isCreatorTask(currentUserId, taskId)) {
			throw new ForbiddenException("User do not have permission");
		}

		// Assigner is already exist in task
		boolean delivered = taskAssignmentRepository.exists(Specification
				.where(TaskAssignSpecification.getByUserAssign(idEncoder.decode(taskAssignRequest.getToUserId()))
						.and(TaskAssignSpecification.getByTask(idEncoder.decode(taskId)))));
		if (delivered) {
			throw new DuplicateException("User already exist in task");
		}

		// Get task
		Task task = taskHelperService.getTask(taskId).orElseThrow(() -> {
			throw new ResourceNotFoundException("Task can not find");
		});

		// Assigner have in team
		Long assignerId = idEncoder.decode(taskAssignRequest.getToUserId());
		if (!teamMemberHelperService.isMemberTeam(assignerId, idEncoder.encode(task.getTeam().getId()))) {
			throw new ResourceNotFoundException("Assigner do not have in team");
		}

		User user = userHelperService.getUser(assignerId).get();

		TaskAssignment taskAssignment = taskAssignmentRepository.save(TaskAssignment.builder().task(task)
				.user(User.builder().id(idEncoder.decode(taskAssignRequest.getToUserId())).build())
				.role(RoleEnum.MEMBER).build());

		sendNotificationAssign(currentUserId, user.getId(), taskId, taskAssignRequest.getTaskTitle(),
				taskAssignRequest.getSenderName(), user.getToken() == null ? null : user.getToken(), user.getLanguage(),
				NotificationType.NEW_ASSIGN.toString());

		return new TaskAssignResponse(idEncoder.encode(taskAssignment.getId()), taskAssignRequest.getToUserId(),
				user.getFirstName() + " " + user.getLastName(), user.getMention(), user.getImage(),
				RoleEnum.MEMBER.toString(),
				ConvertData.timeStampToString(taskAssignment.getJoinedAt()));
	}

	@Override
	public Boolean removeAssign(String taskId, String assignId, TaskAssignRequest taskAssignRequest) {
		Long currentUserId = authService.getUserIdInContext();
		if (!taskHelperService.isCreatorTask(currentUserId, taskId)) {
			return false;
		}
		taskAssignmentRepository.findById(idEncoder.decode(assignId)).ifPresent(taskAssign -> {
			taskAssignmentRepository.deleteById(idEncoder.decode(assignId));
			Optional.ofNullable(userHelperService.getUser(taskAssign.getUser().getId()).get()).ifPresent(user -> {
				sendNotificationAssign(currentUserId, user.getId(), taskId, taskAssignRequest.getTaskTitle(),
						taskAssignRequest.getSenderName(), user.getToken() == null ? null : user.getToken(),
						user.getLanguage(), NotificationType.REMOVE_ASSIGN.toString());
			});
		});
		return true;
	}

	@Override
	public List<TaskAssignResponse> getTaskAssigns(String taskId) {
		return taskAssignmentRepository.getTaskAssignments(idEncoder.decode(taskId)).stream().map(taskAssignment -> {
			String username = Optional.ofNullable(taskAssignment.getFirstName()).orElse("") + " "
					+ Optional.ofNullable(taskAssignment.getLastName()).orElse("");
			String image = Optional.ofNullable(taskAssignment.getImage()).orElse("");
			return new TaskAssignResponse(idEncoder.encode(taskAssignment.getId()),
					idEncoder.encode(taskAssignment.getAssignerId()), username, taskAssignment.getMention(), image,
					taskAssignment.getRole(),
					ConvertData.timeStampToString(taskAssignment.getJoinedAt()));
		}).toList();
	}

	protected void sendNotificationAssign(Long senderId, Long receiverId, String taskId, String taskTitle,
			String senderName, String token, String language, String contentType) {
		applicationEventPublisher.publishEvent(new TaskEvent(idEncoder.decode(taskId), senderId, receiverId, senderName,
				taskTitle, contentType, token, language));
	}
}
