package com.quocbao.taskmanagementsystem.events.DeleteEvent;

import lombok.Getter;

@Getter
public class ReportDeletedEvent {

	private final Long taskId;

	public ReportDeletedEvent(Long taskId) {
		this.taskId = taskId;
	}
}
