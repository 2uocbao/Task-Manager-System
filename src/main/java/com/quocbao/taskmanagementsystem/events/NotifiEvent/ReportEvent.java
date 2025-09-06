package com.quocbao.taskmanagementsystem.events.NotifiEvent;

import lombok.Getter;

@Getter
public class ReportEvent {
	private final Long taskId;
	private final Long senderId;
	private final Long receiverId;
	private final String senderName;
	private final String taskTitle;
	private final String contentType;
	private final String token;
	private final String language;

	public ReportEvent(Long taskId, Long senderId, Long receiverId, String senderName, String taskTitle, String contentType,
			String token, String language) {
		this.taskId = taskId;
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.senderName = senderName;
		this.taskTitle = taskTitle;
		this.contentType = contentType;
		this.token = token;
		this.language = language;
	}
}
