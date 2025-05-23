package com.quocbao.taskmanagementsystem.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.hateoas.EntityModel;
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
import com.quocbao.taskmanagementsystem.payload.request.AssignRequest;
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

	@PostMapping("/tasks")
	public DataResponse createTask(@RequestBody TaskRequest taskRequest) {
		TaskResponse taskResponse = taskService.createTask(taskRequest);
		EntityModel<TaskResponse> entityModel = EntityModel.of(taskResponse);
		return new DataResponse(HttpStatus.OK.value(), entityModel, "Task creation request successful.");
	}

	@GetMapping("/tasks/{taskId}")
	public DataResponse getTask( @PathVariable String taskId) {
		TaskResponse taskResponse = taskService.getTask( taskId);
		return new DataResponse(HttpStatus.OK.value(), taskResponse, "Retrieve info Task.");
	}

	@PutMapping("/tasks/{taskId}")
	public DataResponse updateTask(@PathVariable String taskId, @RequestBody TaskRequest taskRequest) {
		TaskResponse taskResponse = taskService.updateTask(taskId, taskRequest);
		return new DataResponse(HttpStatus.OK.value(), taskResponse, "Update task request successful.");
	}

	@DeleteMapping("/users/{userId}/tasks/{taskId}")
	public DataResponse deleteTask(@PathVariable String userId, @PathVariable String taskId) {
		return new DataResponse(HttpStatus.OK.value(), taskService.deleteTask(userId, taskId), "Request successful.");
	}

	@GetMapping("/users/{userId}/tasks")
	public PaginationResponse<TaskResponse> getTasks(@PathVariable String userId,
			@RequestParam(required = true) String startDate, @RequestParam(required = true) String endDate,
			@RequestParam(required = false, defaultValue = "PENDING") String status,
			@RequestParam(required = false, defaultValue = "LOW") String priority,
			@RequestParam(required = false, defaultValue = "fasle") Boolean assign,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

		Direction direction = Direction.fromString(Direction.DESC.toString());

		Page<TaskResponse> taskPage = taskService.getTasks(userId, status, priority, startDate, endDate, assign,
				PageRequest.of(page, size, Sort.by(direction, "dueAt")));

		List<TaskResponse> entityModels = taskPage.getContent().stream().toList();

		PaginationResponse<TaskResponse> paginationResponse = new PaginationResponse<>(HttpStatus.OK, entityModels,
				taskPage.getPageable().getPageNumber(), taskPage.getSize(), taskPage.getTotalElements(),
				taskPage.getTotalPages(), taskPage.getSort().isSorted(), taskPage.getSort().isUnsorted(),
				taskPage.getSort().isEmpty());

		return paginationResponse;
	}

	@PutMapping("/tasks/{taskId}/assignees")
	public DataResponse asignTaskToUser(@PathVariable String taskId, @RequestBody AssignRequest assignRequest) {
		taskService.addUser(taskId, assignRequest.getUserId(), assignRequest.getToUserId());
		return new DataResponse(HttpStatus.OK.value(), null, "Successful");
	}

	@PostMapping("/tasks/{taskId}/assignees")
	public DataResponse removeUserInTask(@PathVariable String taskId, @RequestBody AssignRequest assignRequest) {
		taskService.removeUser(taskId, assignRequest.getUserId(), assignRequest.getToUserId());
		return new DataResponse(HttpStatus.OK.value(), null, "Successful");
	}

	@PutMapping("/users/{userId}/tasks/{taskId}/status")
	public DataResponse updateStatus(@PathVariable String userId, @PathVariable String taskId,
			@RequestBody TaskRequest taskRequest) {
		return new DataResponse(HttpStatus.OK.value(),
				taskService.updateStatus(taskId, userId, taskRequest), "Success");
	}

	@PutMapping("/users/{userId}/tasks/{taskId}/priority")
	public DataResponse updatePriority(@PathVariable String userId, @PathVariable String taskId,
			@RequestBody TaskRequest taskRequest) {
		return new DataResponse(HttpStatus.OK.value(),
				taskService.updatePriority(taskId, userId, taskRequest), "Success");
	}

	@GetMapping("/users/{userId}/tasks/searchWith")
	public PaginationResponse<TaskResponse> searchTask(@PathVariable String userId, @RequestParam boolean type,
			@RequestParam(required = false) String keySearch, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		Pageable pageable = PageRequest.of(page, size);
		Page<TaskResponse> taskResponses = taskService.searchTasks(userId, keySearch, type, pageable);

		List<TaskResponse> taskResponseList = taskResponses.toList();

		return new PaginationResponse<TaskResponse>(HttpStatus.OK, taskResponseList,
				taskResponses.getPageable().getPageNumber(), taskResponses.getSize(), taskResponses.getTotalElements(),
				taskResponses.getTotalPages(), taskResponses.getSort().isSorted(), taskResponses.getSort().isUnsorted(),
				taskResponses.getSort().isEmpty());
	}
}
