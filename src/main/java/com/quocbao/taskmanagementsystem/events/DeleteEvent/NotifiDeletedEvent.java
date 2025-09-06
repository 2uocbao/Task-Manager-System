package com.quocbao.taskmanagementsystem.events.DeleteEvent;

import lombok.Getter;

@Getter
public class NotifiDeletedEvent {

	private final Long contentId;
	private final String type;

	public NotifiDeletedEvent(Long contentId, String type) {
		this.contentId = contentId;
		this.type = type;
	}
}
