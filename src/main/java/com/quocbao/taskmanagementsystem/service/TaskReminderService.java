package com.quocbao.taskmanagementsystem.service;

import java.time.LocalDateTime;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.quocbao.taskmanagementsystem.events.Task.TaskScheduledEvent;

@Service
public class TaskReminderService {

	private final ApplicationEventPublisher applicationEventPublisher;

	public TaskReminderService(
			ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Async("event_notifi")
	@Scheduled(cron = "0 0 8 * * ?")
	public void checkUpComingDeadlines() {
		LocalDateTime currentDate = LocalDateTime.now().toLocalDate().atStartOfDay();
		LocalDateTime endOfDay = LocalDateTime.now().plusDays(1).toLocalDate().atTime(23, 59, 59);
		applicationEventPublisher.publishEvent(new TaskScheduledEvent(currentDate, endOfDay));
	}

}
