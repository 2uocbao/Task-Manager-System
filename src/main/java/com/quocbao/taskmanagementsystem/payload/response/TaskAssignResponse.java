package com.quocbao.taskmanagementsystem.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TaskAssignResponse {

	@JsonProperty("id")
	private String id;

	@JsonProperty("assigner_id")
	private String assignerId;

	@JsonProperty("name")
	private String name;

	@JsonProperty("mention")
	private String mention;

	@JsonProperty("image")
	private String image;

	@JsonProperty("role")
	private String role;

	@JsonProperty("joined_at")
	private String joinedAt;

	public TaskAssignResponse(String id, String assignerId, String name, String mention, String image, String role,
			String joinedAt) {
		this.id = id;
		this.assignerId = assignerId;
		this.name = name;
		this.mention = mention;
		this.image = image;
		this.role = role;
		this.joinedAt = joinedAt;
	}
}
