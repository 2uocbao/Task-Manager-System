package com.quocbao.taskmanagementsystem.events;

import lombok.Getter;

@Getter
public class TaskEvent {

	private final Long taskId;
	private final Long senderId;
	private final Long receiverId;
	private final String senderName;
	private final String taskTitle;
	private final String contentType;

	public TaskEvent(Long taskId, Long senderId, Long receiverId, String senderName, String taskTitle,
			String contentType) {
		this.taskId = taskId;
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.senderName = senderName;
		this.taskTitle = taskTitle;
		this.contentType = contentType;
	}
}
