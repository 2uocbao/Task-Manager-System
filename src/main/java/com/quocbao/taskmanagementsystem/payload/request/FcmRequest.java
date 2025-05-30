package com.quocbao.taskmanagementsystem.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class FcmRequest {

	@JsonProperty("userId")
	private String userId;
	
	@JsonProperty("fcmToken")
	private String fcmToken;
	
	@JsonProperty("language")
	private String language;
}
