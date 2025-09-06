package com.quocbao.taskmanagementsystem.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quocbao.taskmanagementsystem.common.DataResponse;
import com.quocbao.taskmanagementsystem.common.PaginationResponse;
import com.quocbao.taskmanagementsystem.payload.request.TaskRequest;
import com.quocbao.taskmanagementsystem.payload.response.TaskResponse;
import com.quocbao.taskmanagementsystem.service.TaskService;

@RestController
@RequestMapping()
public class TaskController {

	private TaskService taskService;

	public TaskController(TaskService taskSerivce) {
		this.taskService = taskSerivce;
	}

	@PostMapping("/teams/{teamId}/tasks")
	public DataResponse createTask(@PathVariable String teamId, @RequestBody TaskRequest taskRequest) {
		TaskResponse taskResponse = taskService.createTask(teamId, taskRequest);
		return new DataResponse(HttpStatus.OK.value(), taskResponse, "Task creation request successful.");
	}

	@GetMapping("/tasks/{taskId}")
	public DataResponse getTask(@PathVariable String taskId) {
		TaskResponse taskResponse = taskService.getTask(taskId);
		return new DataResponse(HttpStatus.OK.value(), taskResponse, "Retrieve info Task.");
	}

	@PutMapping("/tasks/{taskId}")
	public DataResponse updateTask(@PathVariable String taskId, @RequestBody TaskRequest taskRequest) {
		TaskResponse taskResponse = taskService.updateTask(taskId, taskRequest);
		return new DataResponse(HttpStatus.OK.value(), taskResponse, "Update task request successful.");
	}

	@DeleteMapping("/tasks/{taskId}")
	public DataResponse deleteTask(@PathVariable String taskId) {
		taskService.deleteTask(taskId);
		return new DataResponse(HttpStatus.OK.value(), null, "Request successful.");
	}

	@GetMapping("/teams/{teamId}/tasks")
	public PaginationResponse<TaskResponse> getTasks(@PathVariable String teamId,
			@RequestParam(required = true) String startDate, @RequestParam(required = true) String endDate,
			@RequestParam(required = false, defaultValue = "PENDING") String status,
			@RequestParam(required = false, defaultValue = "LOW") String priority,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

		Direction direction = Direction.fromString(Direction.DESC.toString());

		Page<TaskResponse> taskPage = taskService.getTasks(teamId, status, priority, startDate, endDate,
				PageRequest.of(page, size, Sort.by(direction, "dueAt")));

		List<TaskResponse> entityModels = taskPage.getContent();

		PaginationResponse<TaskResponse> paginationResponse = new PaginationResponse<>(HttpStatus.OK, entityModels,
				taskPage.getPageable().getPageNumber(), taskPage.getSize(), taskPage.getTotalElements(),
				taskPage.getTotalPages(), taskPage.getSort().isSorted(), taskPage.getSort().isUnsorted(),
				taskPage.getSort().isEmpty());

		return paginationResponse;
	}

	@PutMapping("/tasks/{taskId}/status")
	public DataResponse updateStatus(@PathVariable String taskId, @RequestBody TaskRequest taskRequest) {
		return new DataResponse(HttpStatus.OK.value(), taskService.updateStatus(taskId, taskRequest), "Success");
	}

	@PutMapping("/tasks/{taskId}/priority")
	public DataResponse updatePriority(@PathVariable String taskId, @RequestBody TaskRequest taskRequest) {
		return new DataResponse(HttpStatus.OK.value(), taskService.updatePriority(taskId, taskRequest), "Success");
	}

	@GetMapping("/tasks/searchs")
	public PaginationResponse<TaskResponse> searchTask(
			@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<TaskResponse> taskResponses = taskService.searchTasks(keyword, pageable);

		List<TaskResponse> taskResponseList = taskResponses.getContent();

		return new PaginationResponse<TaskResponse>(HttpStatus.OK, taskResponseList,
				taskResponses.getPageable().getPageNumber(), taskResponses.getSize(), taskResponses.getTotalElements(),
				taskResponses.getTotalPages(), taskResponses.getSort().isSorted(), taskResponses.getSort().isUnsorted(),
				taskResponses.getSort().isEmpty());
	}
}
