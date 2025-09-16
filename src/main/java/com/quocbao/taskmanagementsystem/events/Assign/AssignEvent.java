package com.quocbao.taskmanagementsystem.events.Assign;

import lombok.Getter;

@Getter
public class AssignEvent {

	private final Long taskId;
	private final Long userId;

	public AssignEvent(Long taskId, Long userId) {
		this.taskId = taskId;
		this.userId = userId;
	}
}
