package com.quocbao.taskmanagementsystem.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class RefreshRequest {
	
	@JsonProperty("refresh_token")
	private String refreshToken;

}
