package com.quocbao.taskmanagementsystem.specifications;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.quocbao.taskmanagementsystem.entity.Chat_;
import com.quocbao.taskmanagementsystem.entity.Member;
import com.quocbao.taskmanagementsystem.entity.Member_;
import com.quocbao.taskmanagementsystem.entity.User;

public class MemberSpecification {

	private MemberSpecification() {

	}

	public static Specification<Member> findMemberByGroupId(List<Long> groupIds) {
		return (root, _, _) -> root.get(Member_.chat).get(Chat_.id).in(groupIds);
	}

	public static Specification<Member> findMemberByUserId(long userId) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Member_.user),
				User.builder().id(userId).build());
	}

	public static Specification<Member> excludeUserId(long userId) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.notEqual(root.get(Member_.user),
				User.builder().id(userId).build());
	}

}
