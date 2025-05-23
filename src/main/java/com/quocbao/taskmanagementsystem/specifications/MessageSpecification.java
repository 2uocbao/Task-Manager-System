package com.quocbao.taskmanagementsystem.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.quocbao.taskmanagementsystem.entity.Chat;
import com.quocbao.taskmanagementsystem.entity.Message;
import com.quocbao.taskmanagementsystem.entity.Message_;
import com.quocbao.taskmanagementsystem.entity.User;

public class MessageSpecification {

	private MessageSpecification() {

	}

	public static Specification<Message> getMessagesByGroupId(Chat chat) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Message_.chat), chat);
	}

	public static Specification<Message> getMessageByUserId(User user) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Message_.user), user);
	}

}
