package com.quocbao.taskmanagementsystem.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.quocbao.taskmanagementsystem.entity.Chat;
import com.quocbao.taskmanagementsystem.entity.Chat_;

public class GroupSpecification {

	private GroupSpecification() {

	}

	public static Specification<Chat> search(String keySearch) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.like(root.get(Chat_.name), keySearch);
	}
}
