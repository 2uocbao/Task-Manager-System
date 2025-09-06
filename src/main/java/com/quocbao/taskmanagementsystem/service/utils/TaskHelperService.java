package com.quocbao.taskmanagementsystem.service.utils;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.entity.Task;
import com.quocbao.taskmanagementsystem.repository.TaskRepository;
import com.quocbao.taskmanagementsystem.specifications.TaskSpecification;

@Service
public class TaskHelperService {

	private final TaskRepository taskRepository;
	private final IdEncoder idEncoder;

	public TaskHelperService(TaskRepository taskRepository, IdEncoder idEncoder) {
		this.taskRepository = taskRepository;
		this.idEncoder = idEncoder;
	}

	public Optional<Task> getTask(String taskId) {
		return taskRepository.findById(idEncoder.decode(taskId));
	}

	public Boolean isTaskExist(String taskId) {
		return taskRepository.exists(Specification.where(TaskSpecification.getTaskById(idEncoder.decode(taskId))));
	}

	public Boolean isCreatorTask(Long userId, String taskId) {
		return taskRepository.exists(Specification.where(TaskSpecification.getTaskById(idEncoder.decode(taskId))
				.and(TaskSpecification.getTaskByUserId(userId))));
	}

	public List<Task> getTaskBetweenDate(Timestamp startAt, Timestamp endAt) {
		return taskRepository.findAll(TaskSpecification.getTaskByDate(startAt, endAt));
	}
	
	public List<Task> getTaskByTeamId(String teamId)  {
		return taskRepository.findAll(TaskSpecification.getTaskByTeamId(idEncoder.decode(teamId)));
	}

	public Boolean isTaskActive(String teamId, String status) {
		return taskRepository.exists(Specification.where(TaskSpecification.getTaskByTeamId(idEncoder.decode(teamId))
				.and(TaskSpecification.getTaskNotHaveStatus(status))));
	}
}
