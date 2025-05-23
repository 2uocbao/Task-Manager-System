package com.quocbao.taskmanagementsystem.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class ContactRequest {

	@JsonProperty("from_user")
	private String fromUser;
	
	@JsonProperty("to_user")
	private String toUser;
}
