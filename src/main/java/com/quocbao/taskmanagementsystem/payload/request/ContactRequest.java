package com.quocbao.taskmanagementsystem.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class ContactRequest {

	@JsonProperty("to_user")
	private String toUser;
}
