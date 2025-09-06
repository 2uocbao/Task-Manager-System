package com.quocbao.taskmanagementsystem.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.NotificationType;
import com.quocbao.taskmanagementsystem.entity.Task;
import com.quocbao.taskmanagementsystem.entity.TaskAssignment;
import com.quocbao.taskmanagementsystem.events.NotifiEvent.TaskEvent;
import com.quocbao.taskmanagementsystem.service.utils.TaskAssignmentHelperService;
import com.quocbao.taskmanagementsystem.service.utils.TaskHelperService;

@Service
@Transactional
public class TaskReminderService {
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskReminderService.class);

	private final TaskHelperService taskHelperService;
	private final TaskAssignmentHelperService taskAssignmentHelperService;
	private final IdEncoder idEncoder;
	private final ApplicationEventPublisher applicationEventPublisher;

	public TaskReminderService(TaskHelperService taskHelperService,
			TaskAssignmentHelperService taskAssignmentHelperService, IdEncoder idEncoder,
			ApplicationEventPublisher applicationEventPublisher) {
		this.taskHelperService = taskHelperService;
		this.taskAssignmentHelperService = taskAssignmentHelperService;
		this.idEncoder = idEncoder;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Async("eventTaskExecutor")
	@Scheduled(cron = "0 0 8 * * ?")
	public void checkUpComingDeadlines() {
		LocalDateTime currentDate = LocalDateTime.now().toLocalDate().atStartOfDay();
		LocalDateTime endOfDay = LocalDateTime.now().plusDays(1).toLocalDate().atTime(23, 59, 59);

		List<Task> tasks = taskHelperService.getTaskBetweenDate(Timestamp.valueOf(currentDate),
				Timestamp.valueOf(endOfDay));
		tasks.stream().forEach(task -> {
			pushTaskEventForLeader(task);
			List<TaskAssignment> taskAssignments = taskAssignmentHelperService
					.getAssignmentsByTaskId(idEncoder.encode(task.getId()));
			pushTaskEventForMember(task, taskAssignments);
		});
	}

	protected void pushTaskEventForLeader(Task task) {
		pushTaskEvent(task.getId(), task.getUser().getId(), task.getTitle(), task.getUser().getToken(),
				task.getUser().getLanguage());
	}

	protected void pushTaskEventForMember(Task task, List<TaskAssignment> taskAssignments) {
		taskAssignments.stream().forEach(taskAssign -> {
			pushTaskEvent(task.getId(), taskAssign.getUser().getId(), task.getTitle(), taskAssign.getUser().getToken(),
					taskAssign.getUser().getLanguage());
		});
	}

	protected void pushTaskEvent(Long taskId, Long userId, String title, String token, String language) {
		applicationEventPublisher.publishEvent(
				new TaskEvent(taskId, 0L, userId, null, title, NotificationType.DUEAT.toString(), token, language));
		LOGGER.info("Push notification to " + userId);
	}

}
