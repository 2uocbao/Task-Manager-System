package com.quocbao.taskmanagementsystem.payload.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TaskDetailResponse {

	@JsonProperty("task")
	private TaskResponse taskResponse;

	@JsonProperty("assigners")
	private List<TaskAssignResponse> taskAssignResponse;

	@JsonProperty("reports")
	private List<ReportResponse> reportResponse;

	@JsonProperty("comments")
	private List<CommentResponse> commentResponse;

	public TaskDetailResponse(TaskResponse taskResponse, List<TaskAssignResponse> taskAssignResponse,
			List<ReportResponse> reportResponse, List<CommentResponse> commentResponse) {
		this.taskResponse = taskResponse;
		this.taskAssignResponse = taskAssignResponse;
		this.reportResponse = reportResponse;
		this.commentResponse = commentResponse;
	}
}
