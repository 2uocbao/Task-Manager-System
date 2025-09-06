package com.quocbao.taskmanagementsystem.events.DeleteEvent;

import lombok.Getter;

@Getter
public class NotifiLeavedEvent {

	private final Long userId;
	private final String type;

	public NotifiLeavedEvent(Long userId, String type) {
		this.userId = userId; 
		this.type = type;
	}
}
