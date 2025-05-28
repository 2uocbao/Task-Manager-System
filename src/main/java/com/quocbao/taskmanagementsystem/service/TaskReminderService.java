package com.quocbao.taskmanagementsystem.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.NotificationType;
import com.quocbao.taskmanagementsystem.entity.Task;
import com.quocbao.taskmanagementsystem.events.TaskEvent;
import com.quocbao.taskmanagementsystem.service.utils.TaskHelperService;

@Service
@Transactional
public class TaskReminderService {
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskReminderService.class);

	private final TaskHelperService taskHelperService;
	private final IdEncoder idEncoder;
	private final ApplicationEventPublisher applicationEventPublisher;

	public TaskReminderService(TaskHelperService taskHelperService, IdEncoder idEncoder,
			ApplicationEventPublisher applicationEventPublisher) {
		this.taskHelperService = taskHelperService;
		this.idEncoder = idEncoder;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Scheduled(cron = "0 0 8 * * ?")
	public void checkUpComingDeadlines() {
		LocalDateTime currentDate = LocalDateTime.now().toLocalDate().atStartOfDay();
		LocalDateTime endOfDay = LocalDateTime.now().plusDays(1).toLocalDate().atTime(23, 59, 59);

		List<Task> tasks = taskHelperService.getTaskBetweenDate(Timestamp.valueOf(currentDate),
				Timestamp.valueOf(endOfDay));
		tasks.stream().forEach(task -> {
			if (task.getAssignTo() == null) {
				pushTaskEvent(task, idEncoder.endcode(task.getAssignTo().getId()), NotificationType.DUEAT.toString());
				LOGGER.info("Push notification to " + task.getAssignTo().getId());

			} else {
				pushTaskEvent(task, idEncoder.endcode(task.getAssignTo().getId()), NotificationType.DUEAT.toString());
				LOGGER.info("Push notification to " + task.getUser().getId());
			}
		});
	}

	private void pushTaskEvent(Task task, String assignee, String contentType) {
		applicationEventPublisher.publishEvent(new TaskEvent(task.getId(), null, idEncoder.decode(assignee), "",
				task.getTitle(), NotificationType.DUEAT.toString()));
	}

}
