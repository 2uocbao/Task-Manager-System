package com.quocbao.taskmanagementsystem.service.utils;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.RoleEnum;
import com.quocbao.taskmanagementsystem.entity.Task;
import com.quocbao.taskmanagementsystem.entity.TaskAssignment;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.repository.TaskAssignmentRepository;
import com.quocbao.taskmanagementsystem.specifications.TaskAssignSpecification;

@Service
public class TaskAssignmentHelperService {

	private final TaskAssignmentRepository taskAssignmentRepository;

	private final IdEncoder idEncoder;

	public TaskAssignmentHelperService(TaskAssignmentRepository taskAssignmentRepository, IdEncoder idEncoder) {
		this.taskAssignmentRepository = taskAssignmentRepository;
		this.idEncoder = idEncoder;
	}

	public TaskAssignment addTeamMember(Long userId, Long taskId) {
		TaskAssignment taskAssignment = TaskAssignment.builder().user(User.builder().id(userId).build())
				.task(Task.builder().id(taskId).build()).role(RoleEnum.ADMIN).build();
		return taskAssignmentRepository.save(taskAssignment);
	}

	public Boolean isUserInTask(Long userId, String taskId) {
		return taskAssignmentRepository
				.exists(Specification.where(TaskAssignSpecification.getByTask(idEncoder.decode(taskId)))
						.and(TaskAssignSpecification.getByUserAssign(userId)));
	}

	public Boolean haveAssign(String taskId) {
		return taskAssignmentRepository
				.exists(Specification.where(TaskAssignSpecification.getByTask(idEncoder.decode(taskId))));
	}

	public List<TaskAssignment> getAssignmentsByTaskId(String taskId) {
		return taskAssignmentRepository
				.findAll(Specification.where(TaskAssignSpecification.getByTask(idEncoder.decode(taskId))));
	}

	public Boolean isInAnyTask(Long userId) {
		return taskAssignmentRepository.exists(Specification.where(TaskAssignSpecification.getByUserAssign(userId)));
	}
}
