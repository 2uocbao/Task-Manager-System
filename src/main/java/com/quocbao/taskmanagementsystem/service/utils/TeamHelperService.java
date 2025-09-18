package com.quocbao.taskmanagementsystem.service.utils;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.entity.Team;
import com.quocbao.taskmanagementsystem.repository.TeamRepository;
import com.quocbao.taskmanagementsystem.specifications.TeamSpecification;

@Service
public class TeamHelperService {

	private final TeamRepository teamRepository;

	public TeamHelperService(TeamRepository teamRepository) {
		this.teamRepository = teamRepository;
	}

	public Boolean isLeaderOfTeam(Long userId, Long teamId) {
		return teamRepository.exists(Specification.where(TeamSpecification.getTeamById(teamId))
				.and(TeamSpecification.getTeamByLeaderId(userId)));
	}

	public Team getTeamById(Long teamId) {
		return teamRepository.findById(teamId).get();
	}
}
