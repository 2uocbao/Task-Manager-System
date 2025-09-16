package com.quocbao.taskmanagementsystem.events.Assign;

import lombok.Getter;

@Getter
public class HaveDeadlineEvent {

    private final Long taskId;

    public HaveDeadlineEvent(Long taskId) {
        this.taskId = taskId;
    }
}
