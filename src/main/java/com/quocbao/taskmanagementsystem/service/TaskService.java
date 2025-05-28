package com.quocbao.taskmanagementsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.quocbao.taskmanagementsystem.payload.request.TaskRequest;
import com.quocbao.taskmanagementsystem.payload.response.TaskResponse;

public interface TaskService {

	public TaskResponse createTask(TaskRequest taskRequest);

	public TaskResponse getTask(String userId, String taskId);

	public TaskResponse updateTask(String taskId, TaskRequest taskRequest);

	public String updateStatus(String taskId, String userId, TaskRequest taskRequest);

	public String updatePriority(String taskId, String userId, TaskRequest taskRequest);

	public String deleteTask(String taskId, String userId);

	public Page<TaskResponse> getTasks(String userId, String status, String priority, String startDate, String endDate,
			Boolean assign, Pageable pageable);

	public TaskResponse addUser(String taskId, String assigneer, String assignee);

	public TaskResponse removeUser(String taskId, String assigneer, String assignee);

	public Page<TaskResponse> searchTasks(String userId, String keySearch, Boolean type, Pageable pageable);
}
