package com.quocbao.taskmanagementsystem.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.quocbao.taskmanagementsystem.entity.Notification;
import com.quocbao.taskmanagementsystem.entity.Notification_;

public class NotificationSpecification {

	private NotificationSpecification() {

	}

	public static Specification<Notification> getNotificationsByReceiverId(Long userId) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Notification_.receiverId), userId);
	}

	public static Specification<Notification> getNotificationsByContentId(Long contentId) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Notification_.contentId), contentId);
	}

	public static Specification<Notification> getNotificationByType(String type) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Notification_.type), type);
	}

	public static Specification<Notification> getNotificationByStatus(Boolean status) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Notification_.isRead), status);
	}
}
