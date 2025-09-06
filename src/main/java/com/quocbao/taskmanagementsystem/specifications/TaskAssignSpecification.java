package com.quocbao.taskmanagementsystem.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.quocbao.taskmanagementsystem.entity.TaskAssignment;
import com.quocbao.taskmanagementsystem.entity.TaskAssignment_;
import com.quocbao.taskmanagementsystem.entity.Task_;
import com.quocbao.taskmanagementsystem.entity.User_;

public class TaskAssignSpecification {

	private TaskAssignSpecification() {

	}

	public static Specification<TaskAssignment> getByUserAssign(Long userId) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(TaskAssignment_.user).get(User_.id),
				userId);
	}

	public static Specification<TaskAssignment> getByTask(Long taskId) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(TaskAssignment_.task).get(Task_.id),
				taskId);
	}
}
