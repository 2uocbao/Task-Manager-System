package com.quocbao.taskmanagementsystem.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.quocbao.taskmanagementsystem.common.DataResponse;
import com.quocbao.taskmanagementsystem.common.PaginationResponse;
import com.quocbao.taskmanagementsystem.payload.request.TaskAssignRequest;
import com.quocbao.taskmanagementsystem.payload.response.TaskAssignResponse;
import com.quocbao.taskmanagementsystem.service.TaskAssignService;

@RestController
public class TaskAssignmentController {

	private final TaskAssignService taskAssignService;

	public TaskAssignmentController(TaskAssignService taskAssignService) {
		this.taskAssignService = taskAssignService;
	}

	@PostMapping("/tasks/{taskId}/task_assignments")
	public DataResponse createTaskAssign(@PathVariable String taskId,
			@RequestBody TaskAssignRequest taskAssignRequest) {
		return new DataResponse(HttpStatus.OK.value(), taskAssignService.addAssign(taskId, taskAssignRequest),
				"Success");
	}

	@PutMapping("/tasks/{taskId}/task_assignments/{taskAssignId}")
	public DataResponse deleteTaskAssign(@PathVariable String taskId, @PathVariable String taskAssignId,
			@RequestBody TaskAssignRequest taskAssignRequest) {
		taskAssignService.removeAssign(taskId, taskAssignId, taskAssignRequest);
		return new DataResponse(HttpStatus.OK.value(), null, "Success");
	}

	@GetMapping("/tasks/{taskId}/task_assignments")
	public PaginationResponse<TaskAssignResponse> getTaskAssigns(@PathVariable String taskId) {
		List<TaskAssignResponse> taskAssignResponses = taskAssignService.getTaskAssigns(taskId);
		return new PaginationResponse<>(HttpStatus.OK, taskAssignResponses);
	}
}
