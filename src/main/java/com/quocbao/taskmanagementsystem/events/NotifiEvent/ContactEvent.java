package com.quocbao.taskmanagementsystem.events.NotifiEvent;

import lombok.Getter;

@Getter
public class ContactEvent {

	private final Long senderId;
	private final Long receiverId;
	private final Long contactId;
	private final String senderName;
	private final String contentType;
	private final String language;
	private final String token;

	public ContactEvent(Long senderId, Long receiverId, Long contactId, String senderName, String contentType, String language,
			String token) {
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.contactId = contactId;
		this.senderName = senderName;
		this.contentType = contentType;
		this.language = language;
		this.token = token;
	}
}
