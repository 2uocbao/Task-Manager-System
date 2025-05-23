package com.quocbao.taskmanagementsystem.payload.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.entity.Message;

import lombok.Setter;

@Setter
public class MessageResponse {

	@JsonProperty("id")
	private String id;

	@JsonProperty("userId")
	private String userId;

	@JsonProperty("content")
	private String content;

	@JsonProperty("createdAt")
	private LocalDateTime createdAt;

	public MessageResponse(Message message) {
		this.id = new IdEncoder().endcode(message.getId());
		this.userId = new IdEncoder().endcode(message.getUser().getId());
		this.content = message.getContent();
		this.createdAt = message.getCreatedAt();
	}

}
