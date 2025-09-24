package com.quocbao.taskmanagementsystem.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.quocbao.taskmanagementsystem.events.Assign.AssignEvent;
import com.quocbao.taskmanagementsystem.events.Assign.HaveDeadlineEvent;
import com.quocbao.taskmanagementsystem.service.TaskAssignService;

@Component
public class AssignmentEventListener {

    private final TaskAssignService taskAssignService;

    public AssignmentEventListener(TaskAssignService taskAssignService) {
        this.taskAssignService = taskAssignService;
    }

    @Async("event_notifi")
    @EventListener
    public void getAssignerHaveDealine(HaveDeadlineEvent HaveDeadlineEvent) {
        taskAssignService.assigneeHaveDeadline(HaveDeadlineEvent.getTaskId());
    }

    @Async("event_notifi")
    @EventListener
    public void addCreatorTask(AssignEvent assignEvent) {
        taskAssignService.createAdminTask(assignEvent.getUserId(), assignEvent.getTaskId());
    }
}
