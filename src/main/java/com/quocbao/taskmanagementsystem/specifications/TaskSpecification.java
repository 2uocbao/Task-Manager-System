package com.quocbao.taskmanagementsystem.specifications;

import java.sql.Timestamp;

import org.springframework.data.jpa.domain.Specification;

import com.quocbao.taskmanagementsystem.entity.Task;
import com.quocbao.taskmanagementsystem.entity.Task_;
import com.quocbao.taskmanagementsystem.entity.Team_;
import com.quocbao.taskmanagementsystem.entity.User_;

public class TaskSpecification {

	private TaskSpecification() {

	}

	public static Specification<Task> getTaskByTeamId(Long teamId) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Task_.team).get(Team_.id), teamId);
	}

	public static Specification<Task> getTaskByUserId(Long user) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Task_.user).get(User_.id), user);
	}

	public static Specification<Task> getTaskByStatus(String status) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Task_.status), status);
	}
	
	public static Specification<Task> getTaskNotHaveStatus(String status) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.notEqual(root.get(Task_.status), status);
	}

	public static Specification<Task> getTaskByType(String type) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Task_.priority), type);
	}

	public static Specification<Task> getTaskByDate(Timestamp startAt, Timestamp endAt) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.between(root.get(Task_.dueAt), startAt, endAt);
	}

	public static Specification<Task> getTaskById(Long taskId) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Task_.id), taskId);
	}
}
