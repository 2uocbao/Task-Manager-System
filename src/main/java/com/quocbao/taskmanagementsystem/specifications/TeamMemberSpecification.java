package com.quocbao.taskmanagementsystem.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.quocbao.taskmanagementsystem.common.RoleEnum;
import com.quocbao.taskmanagementsystem.entity.TeamMember;
import com.quocbao.taskmanagementsystem.entity.TeamMember_;
import com.quocbao.taskmanagementsystem.entity.Team_;
import com.quocbao.taskmanagementsystem.entity.User_;

public class TeamMemberSpecification {

	private TeamMemberSpecification() {

	}

	public static Specification<TeamMember> getTeamMemberById(Long id) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(TeamMember_.id), id);
	}

	public static Specification<TeamMember> getTeamMemberByUserId(Long userId) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(TeamMember_.user).get(User_.id), userId);
	}

	public static Specification<TeamMember> getTeamMemberByTeamId(Long teamId) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(TeamMember_.team).get(Team_.id), teamId);
	}

	public static Specification<TeamMember> getTeamMemberByRole(RoleEnum role) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(TeamMember_.role), role);
	}

}
