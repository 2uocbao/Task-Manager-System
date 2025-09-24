package com.quocbao.taskmanagementsystem.service;

import java.util.List;

import com.quocbao.taskmanagementsystem.payload.request.TaskAssignRequest;
import com.quocbao.taskmanagementsystem.payload.response.TaskAssignResponse;

public interface TaskAssignService {

	public TaskAssignResponse addAssign(String taskId, TaskAssignRequest taskAssignRequest);

	public void removeAssign(String taskId, String assignId, TaskAssignRequest taskAssignRequest);

	public List<TaskAssignResponse> getTaskAssigns(String taskId);

	public void assigneeHaveDeadline(Long taskId);

	public void createAdminTask(Long userId, Long taskId);
}
