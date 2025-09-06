package com.quocbao.taskmanagementsystem.payload.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {

	@JsonProperty("sender_name")
	private String senderName;

	@JsonProperty("task_title")
	private String taskTitle;

	@JsonProperty("mention")
	private List<String> mention;

	@JsonProperty("text")
	private String text;
}
