package com.quocbao.taskmanagementsystem.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.entity.User_;

public class UserSpecification {

	private UserSpecification() {
	}

	public static Specification<User> getUserById(Long userId) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(User_.id), userId);
	}
}
