package com.quocbao.taskmanagementsystem.payload.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class MessageRequest {

	private String content;
	private String sender;
	private String receiver;
	private String userSender;

}
