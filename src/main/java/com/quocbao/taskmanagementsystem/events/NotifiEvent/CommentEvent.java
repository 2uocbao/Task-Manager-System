package com.quocbao.taskmanagementsystem.events.NotifiEvent;

import lombok.Getter;

@Getter
public class CommentEvent {

	private final Long senderId;
	private final Long receiverId;
	private final Long contentId;
	private final String senderName;
	private final String titleTask;
	private final String contentType;
	private final String token;
	private final String language;

	public CommentEvent(Long senderId, Long receiverId, Long contentId, String senderName, String titleTask,
			String contentType, String token, String language) {
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.contentId = contentId;
		this.senderName = senderName;
		this.titleTask = titleTask;
		this.contentType = contentType;
		this.token = token;
		this.language = language;
	}
}
