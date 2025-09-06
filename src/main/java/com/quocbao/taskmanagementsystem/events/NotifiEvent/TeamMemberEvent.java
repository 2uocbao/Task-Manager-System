package com.quocbao.taskmanagementsystem.events.NotifiEvent;

import lombok.Getter;

@Getter
public class TeamMemberEvent {

	private final Long senderId;
	private final Long receiverId;
	private final Long contentId;
	private final String senderName;
	private final String teamName;
	private final String contentType;
	private final String token;
	private final String language;
	
	public TeamMemberEvent(Long senderId, Long receiverId, Long contentId, String senderName, String teamName, String contentType, String token, String language) {
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.contentId = contentId;
		this.senderName = senderName;
		this.teamName = teamName;
		this.contentType = contentType;
		this.token = token;
		this.language = language;
	}
}
