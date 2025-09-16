package com.quocbao.taskmanagementsystem.events.Assign;

import lombok.Getter;

@Getter
public class HaveReportEvent {

    private Long userId;
    private Long taskId;

    public HaveReportEvent(Long userId, Long taskId) {
        this.userId = userId;
        this.taskId = taskId;
    }
}
