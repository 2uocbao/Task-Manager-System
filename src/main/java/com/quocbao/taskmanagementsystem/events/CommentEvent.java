package com.quocbao.taskmanagementsystem.events;

import lombok.Getter;

@Getter
public class CommentEvent {

	private final Long senderId;
	private final Long receiverId;
	private final Long contentId;
	private final String senderName;
	private final String titleTask;

	public CommentEvent(Long senderId, Long receiverId, Long contentId, String senderName, String titleTask) {
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.contentId = contentId;
		this.senderName = senderName;
		this.titleTask = titleTask;
	}
}
