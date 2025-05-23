package com.quocbao.taskmanagementsystem.payload.response;

import java.sql.Timestamp;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChatResponse {

	@JsonProperty("id")
	private long id;

	@JsonProperty("name")
	private String name;

	@JsonProperty("lastMessage")
	private String lastMessage;

	@JsonProperty("time")
	private Timestamp time;

	public ChatResponse(long id, String name, String message, Timestamp timeMessage) {
		this.id = id;
		this.name = name;
		this.lastMessage = message;
		this.time = timeMessage;
	}
}
