package com.quocbao.taskmanagementsystem.payload.response;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quocbao.taskmanagementsystem.common.ConvertData;
import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.entity.Task;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskResponse {

	@JsonProperty("id")
	private String id;

	@JsonProperty("user_id")
	private String userId;

	@JsonProperty("assign_to")
	private String assignTo;

	@JsonProperty("imageAssigner")
	private String image;
	
	@JsonProperty("usernameAssigner")
	private String username;

	@JsonProperty("title")
	private String title;

	@JsonProperty("description")
	private String description;

	@JsonProperty("priority")
	private String priority;

	@JsonProperty("status")
	private String status;
	
	@JsonProperty("start_date")
	private String startDate;

	@JsonProperty("created_at")
	private String createdAt;

	@JsonProperty("updated_at")
	private String updatedAt;

	@JsonProperty("due_at")
	private String dueAt;

	@JsonProperty("commentCount")
	private Long commentCount;

	@JsonProperty("reportCount")
	private Long reportCount;

	public TaskResponse() {

	}

	public TaskResponse(String id, String title, String status, Timestamp dueAt, Long commentCount, Long reportCount) {
		this.id = id;
		this.title = title;
		this.status = status;
		this.dueAt = ConvertData.timeStampToString(dueAt);
		this.commentCount = commentCount;
		this.reportCount = reportCount;
	}

	public TaskResponse(Task task) {
		this.id = new IdEncoder().endcode(task.getId());
		this.title = task.getTitle();
		this.description = task.getDescription();
		this.priority = task.getPriority().toString();
		this.status = task.getStatus().toString();
		this.createdAt = ConvertData.timeStampToString(task.getCreatedAt());
		this.updatedAt = ConvertData.timeStampToString(task.getUpdatedAt());
		this.startDate = task.getStartDate() != null ? ConvertData.timeStampToString(task.getStartDate()) : null;
		this.dueAt = ConvertData.timeStampToString(task.getDueAt());
	}
}
