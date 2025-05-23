package com.quocbao.taskmanagementsystem.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class TaskRequest {
	
	@JsonProperty("creatorId")
	private String creatorId;

	@JsonProperty("title")
	private String title;

	@JsonProperty("description")
	private String description;

	@JsonProperty("priority")
	private String priority;
	
	@JsonProperty("status")
	private String status;
	@JsonProperty("due_at")
	private String dueAt;
}
