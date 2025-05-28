package com.quocbao.taskmanagementsystem.service.utils;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

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

	
	public Optional<Task> existTask(String taskId) {
		return taskRepository.findById(idEncoder.decode(taskId));
	}

	
	public List<Task> getTaskBetweenDate(Timestamp startAt, Timestamp endAt) {
		return taskRepository.findAll(TaskSpecification.getTaskByDate(startAt, endAt));
	}
}
