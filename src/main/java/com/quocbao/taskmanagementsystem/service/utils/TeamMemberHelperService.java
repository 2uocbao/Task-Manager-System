package com.quocbao.taskmanagementsystem.service.utils;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.repository.TeamMemberRepository;
import com.quocbao.taskmanagementsystem.specifications.TeamMemberSpecification;

@Service
public class TeamMemberHelperService {

	private final TeamMemberRepository teamMemberRepository;

	private final IdEncoder idEncoder;

	public TeamMemberHelperService(TeamMemberRepository teamMemberRepository, IdEncoder idEncoder) {
		this.teamMemberRepository = teamMemberRepository;
		this.idEncoder = idEncoder;
	}

	public Boolean isMemberTeam(Long userId, String teamId) {
		return teamMemberRepository
				.exists(Specification.where(TeamMemberSpecification.getTeamMemberByUserId(userId)
						.and(TeamMemberSpecification.getTeamMemberByTeamId(idEncoder.decode(teamId)))));
	}
}
