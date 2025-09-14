package com.quocbao.taskmanagementsystem.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quocbao.taskmanagementsystem.common.DataResponse;
import com.quocbao.taskmanagementsystem.common.PaginationResponse;
import com.quocbao.taskmanagementsystem.payload.request.TeamMemberRequest;
import com.quocbao.taskmanagementsystem.payload.response.TeamMemberResponse;
import com.quocbao.taskmanagementsystem.service.TeamMemberService;

@RestController
public class TeamMemberController {

	private final TeamMemberService teamMemberService;

	public TeamMemberController(TeamMemberService teamMemberService) {
		this.teamMemberService = teamMemberService;
	}

	@PostMapping("/teams/{teamId}/team_members")
	public DataResponse createTeamMember(@PathVariable String teamId,
			@RequestBody TeamMemberRequest teamMemberRequest) {
		return new DataResponse(HttpStatus.OK.value(), teamMemberService.createTeamMember(teamId, teamMemberRequest),
				"Success");
	}

	@PutMapping("/teams/{teamId}/team_members/{teamMemberId}")
	public DataResponse deleteTeamMember(@PathVariable String teamId, @PathVariable String teamMemberId) {
		teamMemberService.deleteTeamMember(teamId, teamMemberId);
		return new DataResponse(HttpStatus.OK.value(), null, "Success");
	}

	@GetMapping("/teams/{teamId}/team_members")
	public PaginationResponse<TeamMemberResponse> getTeamMembers(@PathVariable String teamId) {
		Page<TeamMemberResponse> teamMemberPage = teamMemberService.getTeamMembers(teamId, null);
		List<TeamMemberResponse> teamMembers = teamMemberPage.getContent();
		return new PaginationResponse<TeamMemberResponse>(HttpStatus.OK, teamMembers);
	}

	@GetMapping("/teams/{teamId}/team_members/searchs")
	public PaginationResponse<TeamMemberResponse> searchTeamMember(@PathVariable String teamId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String keyword) {
		Pageable pageable = PageRequest.of(page, size);
		Page<TeamMemberResponse> memberResponse = teamMemberService.searchTeamMembers(teamId, keyword, pageable);
		List<TeamMemberResponse> memberResponses = memberResponse.getContent().stream().toList();
		PaginationResponse<TeamMemberResponse> paginationResponse = new PaginationResponse<>(HttpStatus.OK,
				memberResponses, memberResponse.getPageable().getPageNumber(), memberResponse.getSize(),
				memberResponse.getTotalElements(), memberResponse.getTotalPages(), memberResponse.getSort().isSorted(),
				memberResponse.getSort().isUnsorted(), memberResponse.getSort().isEmpty());
		return paginationResponse;
	}

	@PutMapping("/teams/{teamId}/team_members")
	public DataResponse leaveTeam(@PathVariable String teamId) {
		teamMemberService.leaveTeam(teamId);
		return new DataResponse(HttpStatus.OK.value(), null, "Success");
	}

}
