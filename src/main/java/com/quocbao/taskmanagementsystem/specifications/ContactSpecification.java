package com.quocbao.taskmanagementsystem.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.quocbao.taskmanagementsystem.entity.Contacts;
import com.quocbao.taskmanagementsystem.entity.Contacts_;
import com.quocbao.taskmanagementsystem.entity.User_;

public class ContactSpecification {

	private ContactSpecification() {

	}

	public static Specification<Contacts> findContactByUserId(Long userId) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Contacts_.user).get(User_.id), userId);
	}

	public static Specification<Contacts> findContactByStatus(String status) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Contacts_.statusEnum), status);
	}

	public static Specification<Contacts> findContactReceive(Long userId) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Contacts_.friendId).get(User_.id), userId);
	}
}
