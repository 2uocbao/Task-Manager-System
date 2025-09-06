package com.quocbao.taskmanagementsystem.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class TaskAssignRequest {

	@JsonProperty("sender_name")
	private String senderName;

	@JsonProperty("task_title")
	private String taskTitle;

	@JsonProperty("to_userId")
	private String toUserId;
}
