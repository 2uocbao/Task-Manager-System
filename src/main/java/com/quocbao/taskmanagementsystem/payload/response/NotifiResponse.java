package com.quocbao.taskmanagementsystem.payload.response;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quocbao.taskmanagementsystem.common.ConvertData;

import lombok.Setter;

@Setter
public class NotifiResponse {

	@JsonProperty("id")
	private String id;

	@JsonProperty("sender_id")
	private String senderId;

	@JsonProperty("content_id")
	private String contentId;

	@JsonProperty("sender_name")
	private String senderName;

	@JsonProperty("image")
	private String image;

	@JsonProperty("type_content")
	private String typeContent;

	@JsonProperty("title_task")
	private String titleTask;

	@JsonProperty("status")
	private Boolean status;

	@JsonProperty("type")
	private String type;

	@JsonProperty("createdAt")
	private String createdAt;

	public NotifiResponse(Long id, String senderId, String contentId, String senderName, String image,
			String typeContent, String titleTask, Boolean status, String type, Timestamp createdAt) {
		this.id = String.valueOf(id);
		this.senderId = senderId;
		this.contentId = contentId;
		this.senderName = senderName;
		this.image = image;
		this.typeContent = typeContent;
		this.titleTask = titleTask;
		this.status = status;
		this.type = type;
		this.createdAt = createdAt == null ? null : ConvertData.timeStampToString(createdAt);
	}

	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<>();
		map.put("id", id);
		map.put("content_id", contentId);
		map.put("sender_name", senderName);
		map.put("type_content", typeContent);
		map.put("title_task", titleTask);
		map.put("type", type);
		return map;
	}

}
