package com.quocbao.taskmanagementsystem.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Setter;

@Setter
public class TaskStatusResponse {

	@JsonProperty("status")
	private String status;
	
	@JsonProperty("quantity")
	private Long quantity;
	
	public TaskStatusResponse(String status, Long quantity) {
		this.status = status;
		this.quantity = quantity;
	}
}
