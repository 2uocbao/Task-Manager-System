package com.quocbao.taskmanagementsystem.listener;

import java.sql.Timestamp;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.quocbao.taskmanagementsystem.events.Task.TaskScheduledEvent;
import com.quocbao.taskmanagementsystem.service.TaskService;

@Component
public class TaskScheduledListener {

    private final TaskService taskService;

    public TaskScheduledListener(TaskService taskService) {
        this.taskService = taskService;
    }

    @Async("event_notifi")
    @EventListener
    public void retreiveTaskScheduled(TaskScheduledEvent taskScheduledEvent) {
        taskService.listSchudeledTasks(Timestamp.valueOf(taskScheduledEvent.getStartDate()),
                Timestamp.valueOf(taskScheduledEvent.getEndOfDate()));
    }
}
