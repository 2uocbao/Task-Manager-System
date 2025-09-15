package com.quocbao.taskmanagementsystem.service.utils;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.common.RoleEnum;
import com.quocbao.taskmanagementsystem.entity.TaskAssignment;
import com.quocbao.taskmanagementsystem.repository.TaskAssignmentRepository;
import com.quocbao.taskmanagementsystem.specifications.TaskAssignSpecification;

@Service
public class TaskAssignmentHelperService {

	private final TaskAssignmentRepository taskAssignmentRepository;

	public TaskAssignmentHelperService(TaskAssignmentRepository taskAssignmentRepository) {
		this.taskAssignmentRepository = taskAssignmentRepository;
	}

	private Specification customSpecification(Long userId, Long taskId) {
		return Specification.where(TaskAssignSpecification.getByTask(taskId))
				.and(TaskAssignSpecification.getByUserAssign(userId));
	}

	public Boolean isUserInTask(Long userId, Long taskId) {
		return taskAssignmentRepository
				.exists(customSpecification(userId, taskId));
	}

	public Boolean isRoleUserInTask(Long userId, Long taskId, RoleEnum role) {
		return taskAssignmentRepository
				.exists(customSpecification(userId, taskId).and(TaskAssignSpecification.getByRole(role)));
	}

	public Boolean haveAssign(Long taskId) {
		return taskAssignmentRepository
				.exists(Specification.where(TaskAssignSpecification.getByTask(taskId)));
	}

	public List<TaskAssignment> getAssignmentsByTaskId(Long taskId) {
		return taskAssignmentRepository
				.findAll(Specification.where(TaskAssignSpecification.getByTask(taskId)));
	}

	public Boolean isInAnyTask(Long userId) {
		return taskAssignmentRepository.exists(Specification.where(TaskAssignSpecification.getByUserAssign(userId)));
	}

}
