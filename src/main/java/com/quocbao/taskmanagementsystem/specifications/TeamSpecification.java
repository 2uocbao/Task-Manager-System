package com.quocbao.taskmanagementsystem.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.quocbao.taskmanagementsystem.entity.Team;
import com.quocbao.taskmanagementsystem.entity.TeamMember;
import com.quocbao.taskmanagementsystem.entity.Team_;
import com.quocbao.taskmanagementsystem.entity.User_;

import jakarta.persistence.criteria.Join;

public class TeamSpecification {

	private TeamSpecification() {

	}

	public static Specification<Team> getTeamById(Long teamId) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Team_.id), teamId);
	}

	public static Specification<Team> getTeamByLeaderId(Long userId) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Team_.leaderId).get(User_.id), userId);
	}
	
	public static Specification<Team> getTeamByName(String name) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Team_.name), name);
	}
	
	
	public static Specification<Team> searchTeam(String keySearch) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.like(root.get(Team_.name), keySearch);
	}
	
	public static Specification<Team> getTeamByTeamMember(Long userId) {
		return (root, _, criteriaBuilder) ->{
			Join<Team, TeamMember> members = root.join("teamMembers");
            return criteriaBuilder.equal(members.get("user"), userId);
		};
	}
}
