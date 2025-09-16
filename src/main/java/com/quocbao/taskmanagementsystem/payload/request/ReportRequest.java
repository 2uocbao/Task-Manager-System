package com.quocbao.taskmanagementsystem.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequest {

	@JsonProperty("task_id")
	private String taskId;

	@JsonProperty("external_url")
	private String externalUrl;
}
