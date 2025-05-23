package com.quocbao.taskmanagementsystem.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Setter;

@Setter
public class DataResponse {

	@JsonProperty("status")
	private int status;

	@JsonProperty("data")
	private Object data;

	@JsonProperty("message")
	private String message;

	public DataResponse(int status, Object data, String message) {
		this.status = status;
		this.data = data;
		this.message = message;
	}
}
