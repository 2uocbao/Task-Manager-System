package com.quocbao.taskmanagementsystem.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Setter;

@Setter
public class ErrorResponse {

	@JsonProperty("status")
	private int status;

	@JsonProperty("title")
	private String title;

	@JsonProperty("detail")
	private String detail;
}
