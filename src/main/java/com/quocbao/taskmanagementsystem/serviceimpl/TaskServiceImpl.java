package com.quocbao.taskmanagementsystem.serviceimpl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.events.TaskEvent;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.TaskRequest;
import com.quocbao.taskmanagementsystem.payload.response.TaskResponse;
import com.quocbao.taskmanagementsystem.repository.TaskRepository;
import com.quocbao.taskmanagementsystem.service.TaskService;
import com.quocbao.taskmanagementsystem.service.utils.CommentHelperService;
import com.quocbao.taskmanagementsystem.service.utils.ReportHelperService;
import com.quocbao.taskmanagementsystem.service.utils.UserHelperService;
import com.quocbao.taskmanagementsystem.specifications.TaskSpecification;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

	private final TaskRepository taskRepository;

	private final CommentHelperService commentHelperService;

	private final ReportHelperService reportHelperService;

	private final MethodGeneral methodGeneral;

	private final UserHelperService userHelperService;

	private final IdEncoder idEncoder;

	private final ApplicationEventPublisher applicationEventPublisher;

	public TaskServiceImpl(TaskRepository taskRepository, UserHelperService userHelperService,
			MethodGeneral methodGeneral, CommentHelperService commentHelperService,
			ReportHelperService reportHelperService, IdEncoder idEncoder,
			ApplicationEventPublisher applicationEventPublisher) {
		this.taskRepository = taskRepository;
		this.methodGeneral = methodGeneral;
		this.commentHelperService = commentHelperService;
		this.reportHelperService = reportHelperService;
		this.userHelperService = userHelperService;
		this.idEncoder = idEncoder;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Override
	public TaskResponse createTask(TaskRequest taskRequest) {
		User user = userHelperService.userExist(taskRequest.getCreatorId())
				.orElseThrow(() -> new ResourceNotFoundException("User can not found"));
		Task task = new Task(taskRequest);
		task.setUser(user);
		return new TaskResponse(taskRepository.save(task));

	}

	@Override
	public TaskResponse getTask(String userId, String taskId) {
		Task task = taskRepository.findById(idEncoder.decode(taskId))
				.orElseThrow(() -> new ResourceNotFoundException("Task not found"));
		if (task.getAssignTo() != null) {
			methodGeneral.havePermission(idEncoder.decode(userId), task.getUser().getId(), task.getAssignTo().getId());
		} else {
			methodGeneral.validatePermission(idEncoder.decode(userId), task.getUser().getId());
		}
		TaskResponse taskResponse = new TaskResponse(task);
		Optional.ofNullable(task.getAssignTo()).ifPresent(assignTo -> {
			String assignName = Optional.ofNullable(assignTo.getFirstName()).orElse("") + " "
					+ Optional.ofNullable(assignTo.getLastName()).orElse("");
			String image = Optional.ofNullable(assignTo.getImage()).orElse("");
			taskResponse.setUsername(assignName.trim());
			taskResponse.setAssignTo(idEncoder.endcode(assignTo.getId()));
			taskResponse.setImage(image);
		});
		taskResponse.setId(idEncoder.endcode(task.getId()));
		taskResponse.setUserId(idEncoder.endcode(task.getUser().getId()));
		return taskResponse;
	}

	@Override
	public TaskResponse updateTask(String taskId, TaskRequest taskRequest) {
		return taskRepository.findById(idEncoder.decode(taskId)).map(task -> {
			methodGeneral.validatePermission(idEncoder.decode(taskRequest.getCreatorId()), task.getUser().getId());
			task = task.updateTask(taskRequest);
			return new TaskResponse(taskRepository.save(task));
		}).orElseThrow(() -> new ResourceNotFoundException("Task not found for update."));
	}

	@Override
	public String deleteTask(String userId, String taskId) {
		return taskRepository.findById(idEncoder.decode(taskId)).map(t -> {
			methodGeneral.validatePermission(idEncoder.decode(userId), t.getUser().getId());
			taskRepository.delete(Task.builder().id(idEncoder.decode(taskId)).build());
			return "Request Successful";
		}).orElseThrow(() -> new ResourceNotFoundException("Not found task need delete."));
	}

	@Override
	public Page<TaskResponse> getTasks(String userId, String status, String priority, String startDate, String endDate,
			Boolean assign, Pageable pageable) {

		if (Boolean.TRUE.equals(assign)) {
			Page<Task> tasks = listTasksAssign(userId, status, priority, startDate, endDate, pageable);
			return result(tasks);
		}
		Page<Task> tasks = listTasksByUser(userId, status, priority, startDate, endDate, pageable);
		return result(tasks);
	}

	private Page<TaskResponse> result(Page<Task> tasks) {
		List<Long> taskIds = tasks.map(Task::getId).toList();
		Map<Long, Long> commentCounts = commentHelperService.countComment(taskIds);
		Map<Long, Long> reportCounts = reportHelperService.countReport(taskIds);
		return customResponse(tasks, commentCounts, reportCounts);
	}

	@Override
	public TaskResponse addUser(String taskId, String assigneer, String assignee) {
		return taskRepository.findById(idEncoder.decode(taskId)).map(task -> {
			methodGeneral.validatePermission(idEncoder.decode(assigneer), task.getUser().getId());
			return new TaskResponse(assignTaskToNewUser(task, assignee));
		}).orElseThrow(() -> new ResourceNotFoundException("Request failed"));

	}

	@Override
	public TaskResponse removeUser(String taskId, String assigneer, String assignee) {
		return taskRepository.findById(idEncoder.decode(taskId)).map(task -> {
			methodGeneral.validatePermission(idEncoder.decode(assigneer), task.getUser().getId());
			return new TaskResponse(removeUserInTask(task, assignee));
		}).orElseThrow(() -> new ResourceNotFoundException("Request failed"));
	}

	@Override
	public String updateStatus(String taskId, String userId, TaskRequest taskRequest) {
		taskRepository.findById(idEncoder.decode(taskId)).map(task -> {
			if (task.getAssignTo() != null) {
				methodGeneral.havePermission(idEncoder.decode(userId), task.getUser().getId(), task.getAssignTo().getId());
			} else {
				methodGeneral.validatePermission(idEncoder.decode(userId), task.getUser().getId());
			}
			task.setStatus(StatusEnum.valueOf(taskRequest.getStatus()));
			taskRepository.save(task);
			return "Success";
		}).orElseThrow(() -> new ResourceNotFoundException("Request failed"));
		return "False";
	}

	@Override
	public String updatePriority(String taskId, String userId, TaskRequest taskRequest) {
		taskRepository.findById(idEncoder.decode(taskId)).map(task -> {
			methodGeneral.validatePermission(idEncoder.decode(userId), task.getUser().getId());
			task.setPriority(PriorityEnum.valueOf(taskRequest.getPriority()));
			taskRepository.save(task);
			return "Success";
		}).orElseThrow(() -> new ResourceNotFoundException("Request failed"));
		return "False";
	}

	@Override
	public Page<TaskResponse> searchTasks(String userId, String keySearch, Boolean type, Pageable pageable) {
		return taskRepository.searchTask(idEncoder.decode(userId), keySearch, type, pageable)
				.map(t -> new TaskResponse(idEncoder.endcode(t.getId()), t.getTitle(), t.getStatus(), t.getDueAt(),
						t.getCommentCount(), t.getReportCount()));
	}

	private Task assignTaskToNewUser(Task task, String assignee) {
		User user = userHelperService.userExist(assignee)
				.orElseThrow(() -> new ResourceNotFoundException("User can not found"));
		task.setAssignTo(user);
		taskRepository.save(task);
		pushTaskEvent(task, assignee, NotificationType.NEW_ASSIGN.toString());
		return task;
	}

	private Task removeUserInTask(Task task, String assignee) {
		if (task.getAssignTo().getId() == idEncoder.decode(assignee)) {
			task.setAssignTo(null);
			task.setStatus(StatusEnum.CANCELLED);
			taskRepository.save(task);
			pushTaskEvent(task, assignee, NotificationType.REMOVE_ASSIGN.toString());
		}
		return task;
	}

	private Page<Task> listTasksAssign(String userId, String status, String priority, String startDate, String endDate,
			Pageable pageable) {
		Specification<Task> specification = Specification.where(TaskSpecification
				// by userId
				.getTaskByUserId(idEncoder.decode(userId))
				// by status
				.and(TaskSpecification.getTaskByStatus(status)
						// by priority
						.and(TaskSpecification.getTaskByType(priority)
								// assignee
								.and(TaskSpecification.getTaskHaveAssign())
								// between custom date
								.and(TaskSpecification.getTaskByDate(ConvertData.toTimestamp(startDate),
										ConvertData.toTimestamp(endDate))))));
		return taskRepository.findAll(specification, pageable);
	}

	private Page<Task> listTasksByUser(String userId, String status, String priority, String startDate, String endDate,
			Pageable pageable) {
		Specification<Task> specification = Specification
				// assigned
				.where(TaskSpecification.getTaskAssign(idEncoder.decode(userId)))
				// of this person not assign
				.or(TaskSpecification.getTaskByUserId(idEncoder.decode(userId))
						.and(TaskSpecification.getTaskNotAssign()))
				.and(TaskSpecification.getTaskByStatus(status)).and(TaskSpecification.getTaskByType(priority))
				.and(TaskSpecification.getTaskByDate(ConvertData.toTimestamp(startDate),
						ConvertData.toTimestamp(endDate)));
		return taskRepository.findAll(specification, pageable);
	}

	private Page<TaskResponse> customResponse(Page<Task> tasks, Map<Long, Long> commentCounts,
			Map<Long, Long> reportCounts) {
		return tasks.map(task -> {
			Long commentCount = commentCounts.getOrDefault(task.getId(), 0L);
			Long reportCount = reportCounts.getOrDefault(task.getId(), 0L);
			TaskResponse taskResponse = new TaskResponse(task);
			taskResponse.setCommentCount(commentCount);
			taskResponse.setReportCount(reportCount);
			return taskResponse;
		});
	}

	private void pushTaskEvent(Task task, String assignee, String contentType) {
		String senderName = Optional.ofNullable(task.getUser())
				.map(user -> Optional.ofNullable(user.getFirstName()).orElse("") + " "
						+ Optional.ofNullable(user.getLastName()).orElse(""))
				.orElse("Unknown User");
		applicationEventPublisher.publishEvent(new TaskEvent(task.getId(), task.getUser().getId(),
				idEncoder.decode(assignee), senderName, task.getTitle(), contentType));
	}
}
