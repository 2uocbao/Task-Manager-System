package com.quocbao.taskmanagementsystem.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {

	@JsonProperty("mention")
	private String mention;
	@JsonProperty("text")
	private String text;
}
