package com.quocbao.taskmanagementsystem.events;

import lombok.Getter;

@Getter
public class ContactEvent {

	private final Long senderId;
	private final Long receiverId;
	private final Long contactId;
	private final String senderName;
	
	public ContactEvent(Long senderId, Long receiverId, Long contactId, String senderName) {
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.contactId = contactId;
		this.senderName = senderName;
	}
}
