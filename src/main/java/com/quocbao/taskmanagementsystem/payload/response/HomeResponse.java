package com.quocbao.taskmanagementsystem.payload.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Setter;

@Setter
public class HomeResponse {

	@JsonProperty("teams")
	private List<TeamResponse> teamResponse;

	@JsonProperty("status_summary")
	private List<TaskStatusResponse> statusSummary;

	@JsonProperty("tasks")
	private List<TaskResponse> tasks;

	public HomeResponse(List<TeamResponse> teamResponse, List<TaskStatusResponse> statusSummary,
			List<TaskResponse> tasks) {
		this.teamResponse = teamResponse;
		this.statusSummary = statusSummary;
		this.tasks = tasks;
	}
}
