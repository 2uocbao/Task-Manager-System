package com.quocbao.taskmanagementsystem.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quocbao.taskmanagementsystem.common.PaginationResponse;
import com.quocbao.taskmanagementsystem.payload.response.CommentResponse;
import com.quocbao.taskmanagementsystem.payload.response.HomeResponse;
import com.quocbao.taskmanagementsystem.payload.response.ReportResponse;
import com.quocbao.taskmanagementsystem.payload.response.TaskAssignResponse;
import com.quocbao.taskmanagementsystem.payload.response.TaskDetailResponse;
import com.quocbao.taskmanagementsystem.payload.response.TaskResponse;
import com.quocbao.taskmanagementsystem.payload.response.TaskStatusResponse;
import com.quocbao.taskmanagementsystem.payload.response.TeamResponse;
import com.quocbao.taskmanagementsystem.service.CommentService;
import com.quocbao.taskmanagementsystem.service.ReportService;
import com.quocbao.taskmanagementsystem.service.TaskAssignService;
import com.quocbao.taskmanagementsystem.service.TaskService;
import com.quocbao.taskmanagementsystem.service.TeamService;

@RestController
public class CustomController {

	private final TeamService teamService;

	private final TaskService taskService;

	private final TaskAssignService taskAssignService;

	private final CommentService commentService;

	private final ReportService reportService;

	public CustomController(TeamService teamService, TaskService taskService, TaskAssignService taskAssignService,
			CommentService commentService, ReportService reportService) {
		this.teamService = teamService;
		this.taskService = taskService;
		this.taskAssignService = taskAssignService;
		this.commentService = commentService;
		this.reportService = reportService;
	}

	@GetMapping("/home")
	public PaginationResponse<HomeResponse> getInformation(@RequestParam(required = false) String teamId,
			@RequestParam(required = true) String startDate, @RequestParam(required = true) String endDate,
			@RequestParam(required = false, defaultValue = "PENDING") String status,
			@RequestParam(required = false, defaultValue = "LOW") String priority,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

		Direction direction = Direction.fromString(Direction.DESC.toString());
		List<TeamResponse> teamResponse = teamService.getCustomTeams();
		List<TaskStatusResponse> taskStatusResponse = null;
		Page<TaskResponse> taskResponse = null;
		String teamIdRequest = teamId;
		if (!teamResponse.isEmpty()) {
			boolean haveTeamId = teamResponse.stream().anyMatch(team -> team.getId().equals(teamId));
			if (!haveTeamId) {
				teamIdRequest = teamResponse.getFirst().getId();
			}
			taskStatusResponse = taskService.getTaskSummaryInTeam(teamIdRequest);
			taskResponse = taskService.getTasks(teamIdRequest, status, priority, startDate, endDate,
					PageRequest.of(page, size, Sort.by(direction, "dueAt")));
		} else {
			return new PaginationResponse<HomeResponse>(HttpStatus.OK, null);
		}

		HomeResponse homeResponse = new HomeResponse(teamResponse, taskStatusResponse, taskResponse.getContent());
		return new PaginationResponse<>(HttpStatus.OK, List.of(homeResponse),
				taskResponse.getPageable().getPageNumber(), taskResponse.getSize(), taskResponse.getTotalElements(),
				taskResponse.getTotalPages(), taskResponse.getSort().isSorted(), taskResponse.getSort().isUnsorted(),
				taskResponse.getSort().isEmpty());
	}

	@GetMapping("/tasks/{taskId}/task_detail")
	public PaginationResponse<TaskDetailResponse> getTaskDetail(@PathVariable String taskId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		Direction direction = Direction.fromString(Direction.DESC.toString());
		TaskResponse taskResponse = taskService.getTask(taskId);
		List<TaskAssignResponse> taskAssignResponses = taskAssignService.getTaskAssigns(taskId);
		List<ReportResponse> reportResponses = reportService.getReports(taskId);
		Page<CommentResponse> commentResponses = commentService.getCommentsofTask(taskId,
				PageRequest.of(page, size, Sort.by(direction, "createdAt")));
		TaskDetailResponse taskDetailResponse = new TaskDetailResponse(taskResponse, taskAssignResponses,
				reportResponses, commentResponses.getContent());
		return new PaginationResponse<>(HttpStatus.OK, List.of(taskDetailResponse),
				commentResponses.getPageable().getPageNumber(), commentResponses.getSize(),
				commentResponses.getTotalElements(), commentResponses.getTotalPages(),
				commentResponses.getSort().isSorted(), commentResponses.getSort().isUnsorted(),
				commentResponses.getSort().isEmpty());
	}
}
