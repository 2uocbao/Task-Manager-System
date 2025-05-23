package com.quocbao.taskmanagementsystem.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class AssignRequest {

	@JsonProperty("user_id")
	private String userId;
	
	@JsonProperty("toUser_id")
	private String toUserId;
}
