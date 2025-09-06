package com.quocbao.taskmanagementsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.quocbao.taskmanagementsystem.payload.request.TeamMemberRequest;
import com.quocbao.taskmanagementsystem.payload.response.TeamMemberResponse;

public interface TeamMemberService {

	public TeamMemberResponse createTeamMember(String teamId, TeamMemberRequest teamMemberRequest);

	public void deleteTeamMember(String teamId, String teamMemberId, TeamMemberRequest teamMemberRequest);

	public Page<TeamMemberResponse> getTeamMembers(String teamId, Pageable pageable);

	public Page<TeamMemberResponse> searchTeamMembers(String teamId, String keyword, Pageable pageable);
	
	public void leaveTeam(String teamId, TeamMemberRequest teamMemberRequest);
}
