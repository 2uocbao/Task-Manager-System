package com.quocbao.taskmanagementsystem.service.utils;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.quocbao.taskmanagementsystem.entity.Task;
import com.quocbao.taskmanagementsystem.repository.TaskRepository;
import com.quocbao.taskmanagementsystem.specifications.TaskSpecification;

@Service
public class TaskHelperService {

	private final TaskRepository taskRepository;

	public TaskHelperService(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	public Optional<Task> getTask(Long taskId) {
		return taskRepository.findById(taskId);
	}

	public Boolean isTaskExist(Long taskId) {
		return taskRepository.exists(Specification.where(TaskSpecification.getTaskById(taskId)));
	}

	public Boolean isCreatorTask(Long userId, Long taskId) {
		return taskRepository.exists(Specification.where(TaskSpecification.getTaskById(taskId)
				.and(TaskSpecification.getTaskByUserId(userId))));
	}

	public List<Task> getTaskBetweenDate(Timestamp startAt, Timestamp endAt) {
		return taskRepository.findAll(TaskSpecification.getTaskByDate(startAt, endAt));
	}

	public List<Task> getTaskByTeamId(Long teamId) {
		return taskRepository.findAll(TaskSpecification.getTaskByTeamId(teamId));
	}

	public Boolean isTaskActive(Long teamId, String status) {
		return taskRepository.exists(Specification.where(TaskSpecification.getTaskByTeamId(teamId)
				.and(TaskSpecification.getTaskNotHaveStatus(status))));
	}
}
