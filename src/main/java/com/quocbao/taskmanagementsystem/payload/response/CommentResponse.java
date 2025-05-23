package com.quocbao.taskmanagementsystem.payload.response;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quocbao.taskmanagementsystem.common.ConvertData;
import com.quocbao.taskmanagementsystem.entity.Comment;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CommentResponse {

	@JsonProperty("id")
	private long id;

	@JsonProperty("creatorId")
	private String creatorId;

	@JsonProperty("username")
	private String username;

	@JsonProperty("image")
	private String image;

	@JsonProperty("text")
	private String text;

	@JsonProperty("created_at")
	private String createdAt;

	@JsonProperty("updated_at")
	private String updatedAt;

	public CommentResponse(Comment comment) {
		this.id = comment.getId();
		this.text = comment.getText();
		this.createdAt = ConvertData.timeStampToString(comment.getCreatedAt());
		this.updatedAt = ConvertData.timeStampToString(comment.getUpdatedAt());
	}

	public CommentResponse(Long id, String text, String userId, String firstName, String lastName, String imagePath,
			Timestamp createdAt) {
		this.id = id;
		this.text = text;
		this.creatorId = userId;
		this.username = firstName + " " + lastName;
		this.image = imagePath;
		this.createdAt = ConvertData.timeStampToString(createdAt);
	}

}
