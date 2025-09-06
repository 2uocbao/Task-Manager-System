package com.quocbao.taskmanagementsystem.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TeamMemberResponse {

	@JsonProperty("id")
	private String id;

	@JsonProperty("member_id")
	private String memberId;

	@JsonProperty("member_name")
	private String memberName;

	@JsonProperty("member_image")
	private String memberImage;

	@JsonProperty("joined_at")
	private String joinedAt;

	public TeamMemberResponse(String id, String memberId, String memberName, String memberImage, String joinedAt) {
		this.id = id;
		this.memberId = memberId;
		this.memberName = memberName;
		this.memberImage = memberImage;
		this.joinedAt = joinedAt;
	}
	
	public TeamMemberResponse(String image) {
		this.memberImage = image;
	}
}
