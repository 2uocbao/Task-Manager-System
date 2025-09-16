package com.quocbao.taskmanagementsystem.events.Task;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class TaskScheduledEvent {

    private final LocalDateTime startDate;
    private final LocalDateTime endOfDate;

    public TaskScheduledEvent(LocalDateTime startDate, LocalDateTime endOfDate) {
        this.startDate = startDate;
        this.endOfDate = endOfDate;
    }
}