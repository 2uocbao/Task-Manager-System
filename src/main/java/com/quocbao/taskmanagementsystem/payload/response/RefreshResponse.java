package com.quocbao.taskmanagementsystem.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Setter;

@Setter
public class RefreshResponse {

	@JsonProperty("token")
	private String asscessToken;

	public RefreshResponse(String accessToken) {
		this.asscessToken = accessToken;
	}
}
