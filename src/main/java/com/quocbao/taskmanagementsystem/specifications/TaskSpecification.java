package com.quocbao.taskmanagementsystem.specifications;

import java.sql.Timestamp;

import org.springframework.data.jpa.domain.Specification;

import com.quocbao.taskmanagementsystem.entity.Task;
import com.quocbao.taskmanagementsystem.entity.Task_;
import com.quocbao.taskmanagementsystem.entity.User_;

public class TaskSpecification {

	private TaskSpecification() {

	}

	public static Specification<Task> getTaskByUserId(long user) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Task_.user).get(User_.id), user);
	}

	public static Specification<Task> getTaskHaveAssign() {
		return (root, _, criteriaBuilder) -> criteriaBuilder.isNotNull(root.get(Task_.assignTo).get(User_.id));
	}

	public static Specification<Task> getTaskNotAssign() {
		return (root, _, criteriaBuilder) -> criteriaBuilder.isNull(root.get(Task_.assignTo).get(User_.id));
	}

	public static Specification<Task> getTaskAssign(Long userId) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Task_.assignTo).get(User_.id), userId);
	}

	public static Specification<Task> getTaskByStatus(String status) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Task_.status), status);
	}

	public static Specification<Task> getTaskByType(String type) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Task_.priority), type);
	}

	public static Specification<Task> getTaskByDate(Timestamp startAt, Timestamp endAt) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.between(root.get(Task_.dueAt), startAt, endAt);
	}

}
