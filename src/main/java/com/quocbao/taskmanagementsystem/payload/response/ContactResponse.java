package com.quocbao.taskmanagementsystem.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContactResponse {

	@JsonProperty("id")
	private String id;
	
	@JsonProperty("user_id")
	private String userId;

	@JsonProperty("username")
	private String username;
	
	@JsonProperty("email")
	private String email;
	
	@JsonProperty("image")
	private String image;

	@JsonProperty("status")
	private String status;

	@JsonProperty("created_at")
	private String createdAt;

	public ContactResponse() {
		
	}
	
	public ContactResponse(String id, String userId, String firstName, String lastName, String email, String image, String status) {
		this.id = id;
		this.userId = userId;
		this.username = firstName + " " + lastName;
		this.image = image;
		this.email = email;
		this.status = status;
	}
}
