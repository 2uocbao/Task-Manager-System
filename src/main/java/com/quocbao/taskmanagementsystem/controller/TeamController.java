package com.quocbao.taskmanagementsystem.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quocbao.taskmanagementsystem.common.DataResponse;
import com.quocbao.taskmanagementsystem.common.PaginationResponse;
import com.quocbao.taskmanagementsystem.payload.request.TeamRequest;
import com.quocbao.taskmanagementsystem.payload.response.TeamResponse;
import com.quocbao.taskmanagementsystem.service.TeamService;

@RestController
public class TeamController {

	private final TeamService teamService;

	public TeamController(TeamService teamService) {
		this.teamService = teamService;
	}

	@PostMapping("/teams")
	public DataResponse createTeam(@RequestBody TeamRequest teamRequest) {
		return new DataResponse(HttpStatus.OK.value(), teamService.createTeam(teamRequest), "Success");
	}

	@PutMapping("/teams/{teamId}")
	public DataResponse updateTeam(@PathVariable String teamId, @RequestBody TeamRequest teamRequest) {
		return new DataResponse(HttpStatus.OK.value(), teamService.updateTeam(teamId, teamRequest), "Success");
	}

	@GetMapping("/teams")
	public PaginationResponse<TeamResponse> getTeams() {
		List<TeamResponse> teamResponses = teamService.getTeams();
		return new PaginationResponse<>(HttpStatus.OK, teamResponses);
	}

	@GetMapping("/teamsCustom")
	public PaginationResponse<TeamResponse> getTeamsCustom() {
		List<TeamResponse> teamResponses = teamService.getCustomTeams();
		return new PaginationResponse<>(HttpStatus.OK, teamResponses);
	}

	@GetMapping("/teams/searchs")
	public PaginationResponse<TeamResponse> searchTeams(@RequestParam String keyword,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<TeamResponse> teamResponseResult = teamService.searchTeams(keyword, pageable);
		return new PaginationResponse<>(HttpStatus.OK, teamResponseResult.getContent(),
				teamResponseResult.getPageable().getPageNumber(), teamResponseResult.getSize(),
				teamResponseResult.getTotalElements(), teamResponseResult.getTotalPages(),
				teamResponseResult.getSort().isSorted(), teamResponseResult.getSort().isUnsorted(),
				teamResponseResult.getSort().isEmpty());
	}

	@DeleteMapping("/teams/{teamId}")
	public DataResponse deleteTeam(@PathVariable String teamId) {
		teamService.deleteTeam(teamId);
		return new DataResponse(HttpStatus.OK.value(), null, "Success");
	}

}
