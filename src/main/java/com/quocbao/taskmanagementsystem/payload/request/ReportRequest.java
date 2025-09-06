package com.quocbao.taskmanagementsystem.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class ReportRequest {

	@JsonProperty("task_id")
	private String taskId;

	@JsonProperty("external_url")
	private String externalUrl;

}
