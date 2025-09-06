package com.quocbao.taskmanagementsystem.service.utils;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.entity.Team;
import com.quocbao.taskmanagementsystem.repository.TeamRepository;
import com.quocbao.taskmanagementsystem.specifications.TeamSpecification;

@Service
public class TeamHelperService {

	private final TeamRepository teamRepository;

	private final IdEncoder idEncoder;

	public TeamHelperService(TeamRepository teamRepository, IdEncoder idEncoder) {
		this.teamRepository = teamRepository;
		this.idEncoder = idEncoder;
	}

	public Boolean isLeaderOfTeam(Long userId, String teamId) {
		return teamRepository.exists(Specification.where(TeamSpecification.getTeamById(idEncoder.decode(teamId)))
				.and(TeamSpecification.getTeamByLeaderId(userId)));
	}
	
	public Team getTeamById(String teamId) {
		return teamRepository.findById(idEncoder.decode(teamId)).get();
	}
}
