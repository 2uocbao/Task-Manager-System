package com.quocbao.taskmanagementsystem.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.quocbao.taskmanagementsystem.payload.request.TaskRequest;
import com.quocbao.taskmanagementsystem.payload.response.TaskResponse;
import com.quocbao.taskmanagementsystem.payload.response.TaskStatusResponse;

public interface TaskService {

	public TaskResponse createTask(String teamId, TaskRequest taskRequest);

	public TaskResponse getTask(String taskId);

	public TaskResponse updateTask(String taskId, TaskRequest taskRequest);

	public TaskResponse updateStatus(String taskId, TaskRequest taskRequest);

	public TaskResponse updatePriority(String taskId, TaskRequest taskRequest);

	public void deleteTask(String taskId);

	public List<TaskStatusResponse> getTaskSummaryInTeam(String teamId);

	public Page<TaskResponse> getTasks(String teamId, String status, String priority, String startDate,
			String endDate, Pageable pageable);

	public Page<TaskResponse> searchTasks(String keyword, Pageable pageable);
}
