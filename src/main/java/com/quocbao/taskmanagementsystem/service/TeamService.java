package com.quocbao.taskmanagementsystem.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.quocbao.taskmanagementsystem.payload.request.TeamRequest;
import com.quocbao.taskmanagementsystem.payload.response.TeamResponse;

public interface TeamService {

	public TeamResponse createTeam(TeamRequest teamRequest);

	public TeamResponse updateTeam(String teamId, TeamRequest teamRequest);
	
	public void deleteTeam(String teamId);

	public List<TeamResponse> getTeams();
	
	public List<TeamResponse> getCustomTeams();
	
	public Page<TeamResponse> searchTeams(String keyword, Pageable pageable);
}
