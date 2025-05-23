package com.quocbao.taskmanagementsystem.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.controller.TaskController;
import com.quocbao.taskmanagementsystem.entity.Task;
import com.quocbao.taskmanagementsystem.payload.request.NotifiRequest;
import com.quocbao.taskmanagementsystem.repository.TaskRepository;
import com.quocbao.taskmanagementsystem.specifications.TaskSpecification;

@Service
@Transactional
public class TaskReminderService {
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskReminderService.class);

	private final TaskRepository taskRepository;
	private final NotificationService notificationService;
	private final UserService userService;
	private final IdEncoder idEncoder;

	public TaskReminderService(TaskRepository taskReposiotry, NotificationService notificationService,
			IdEncoder idEncoder,UserService userService, TaskController taskController) {
		this.taskRepository = taskReposiotry;
		this.notificationService = notificationService;
		this.idEncoder = idEncoder;
		this.userService = userService;
	}

	@Scheduled(cron = "0 0 7 * * ?")
	public void checkUpComingDeadlines() {
		LocalDateTime currentDate = LocalDateTime.now().toLocalDate().atStartOfDay();
		LocalDateTime endOfDay = LocalDateTime.now().plusDays(15).toLocalDate().atTime(23, 59, 59);

		List<Task> tasks = taskRepository
				.findAll(TaskSpecification.getTaskByDate(Timestamp.valueOf(currentDate), Timestamp.valueOf(endOfDay)));
		tasks.stream().forEach(t -> {
			NotifiRequest notifiRequest;
			if (t.getAssignTo() == null) {
				String token = userService.getToken(t.getAssignTo().getId());
				notifiRequest = NotifiRequest.builder().receiverId(idEncoder.endcode(t.getUser().getId())).type("TASK")
						.typeContent("DUEAT").contentId(idEncoder.endcode(t.getId())).tokenFcm(token).build();
			} else {
				String token = userService.getToken(t.getUser().getId());
				notifiRequest = NotifiRequest.builder().receiverId(idEncoder.endcode(t.getAssignTo().getId())).type("TASK")
						.typeContent("DUEAT").contentId(idEncoder.endcode(t.getId())).tokenFcm(token).build();
			}
			LOGGER.info("Send notification to " + notifiRequest.getReceiverId());
			notificationService.createNotification(notifiRequest);
		});
	}

}
