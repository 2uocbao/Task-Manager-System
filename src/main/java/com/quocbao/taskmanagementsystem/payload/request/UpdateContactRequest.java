package com.quocbao.taskmanagementsystem.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class UpdateContactRequest {

	@JsonProperty("sender_name")
	private String senderName;

	@JsonProperty("to_user")
	private String toUser;

	@JsonProperty("status")
	private String status;
}
