package com.quocbao.taskmanagementsystem.payload.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotifiRequest {
	
	private String senderId;
	private String receiverId;
	private String contentId;
	private String senderName;
	private String typeContent;
	private String titleTask;
	private String type;
	private String tokenFcm;
}
