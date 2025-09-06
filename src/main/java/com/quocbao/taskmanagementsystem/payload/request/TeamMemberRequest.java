package com.quocbao.taskmanagementsystem.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class TeamMemberRequest {

	@JsonProperty("member_id")
	private String memberId;

	@JsonProperty("leader_name")
	private String leaderName;

	@JsonProperty("team_name")
	private String teamName;
}
