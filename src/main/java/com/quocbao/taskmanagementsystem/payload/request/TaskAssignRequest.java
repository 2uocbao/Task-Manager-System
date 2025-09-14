package com.quocbao.taskmanagementsystem.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class TaskAssignRequest {
	@JsonProperty("to_userId")
	private String toUserId;
}
