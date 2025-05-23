package com.quocbao.taskmanagementsystem.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.entity.User;

import lombok.Setter;

@Setter
public class UserResponse {

	@JsonProperty("id")
	private String id;

	@JsonProperty("first_name")
	private String firstName;

	@JsonProperty("last_name")
	private String lastName;

	@JsonProperty("mention")
	private String mention;

	@JsonProperty("image")
	private String image;

	@JsonProperty("email")
	private String email;

	public UserResponse() {

	}

	public UserResponse(User user) {
		this.id = new IdEncoder().endcode(user.getId());
		this.firstName = user.getFirstName();
		this.lastName = user.getLastName();
		this.mention = user.getMention();
		this.image = user.getImage();
		this.email = user.getEmail();
	}
	
	public UserResponse(String id, String firstName, String lastName, String image, String email) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.image = image;
		this.email = email;
	}

}
