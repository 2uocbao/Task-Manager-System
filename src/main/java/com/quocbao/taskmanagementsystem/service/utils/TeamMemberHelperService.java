package com.quocbao.taskmanagementsystem.service.utils;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.repository.TeamMemberRepository;
import com.quocbao.taskmanagementsystem.specifications.TeamMemberSpecification;

@Service
public class TeamMemberHelperService {

	private final TeamMemberRepository teamMemberRepository;

	public TeamMemberHelperService(TeamMemberRepository teamMemberRepository) {
		this.teamMemberRepository = teamMemberRepository;
	}

	public Boolean isMemberTeam(Long userId, Long teamId) {
		return teamMemberRepository
				.exists(Specification.where(TeamMemberSpecification.getTeamMemberByUserId(userId)
						.and(TeamMemberSpecification.getTeamMemberByTeamId(teamId))));
	}
}
