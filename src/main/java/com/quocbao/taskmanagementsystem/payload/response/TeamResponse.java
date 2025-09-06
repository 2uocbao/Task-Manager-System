package com.quocbao.taskmanagementsystem.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class TeamResponse {

	@JsonProperty("id")
	private String id;

	@JsonProperty("name")
	private String name;

	@JsonProperty("creator_id")
	private String creatorId;

	@JsonProperty("creator_name")
	private String creatorName;
	
	@JsonProperty("creator_image")
	private String creatorImage;

	@JsonProperty("created_at")
	private String createdAt;

	public TeamResponse(String id, String name, String creatorId, String creatorName, String creatorImage, String createdAt) {
		this.id = id;
		this.name = name;
		this.creatorId = creatorId;
		this.creatorName = creatorName;
		this.creatorImage = creatorImage;
		this.createdAt = createdAt;
	}

	public TeamResponse(String id, String name) {
		this.id = id;
		this.name = name;
	}
}
