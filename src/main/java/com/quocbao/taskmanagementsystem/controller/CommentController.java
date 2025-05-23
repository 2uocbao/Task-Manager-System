package com.quocbao.taskmanagementsystem.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import com.quocbao.taskmanagementsystem.payload.request.CommentRequest;
import com.quocbao.taskmanagementsystem.payload.response.CommentResponse;
import com.quocbao.taskmanagementsystem.service.CommentService;

@RestController
@RequestMapping()
public class CommentController {

	private CommentService commentService;

	public CommentController(CommentService commentService) {
		this.commentService = commentService;
	}

	@PostMapping("/users/{userId}/tasks/{taskId}/comments")
	public DataResponse createcomment(@PathVariable String userId, @PathVariable String taskId,
			@RequestBody CommentRequest commentRequest) {
		return new DataResponse(HttpStatus.OK.value(), commentService.createComment(userId, taskId, commentRequest),
				"Create successful");
	}

	@PutMapping("/users/{userId}/comments/{commentId}")
	public DataResponse updatecomment(@PathVariable String userId, @PathVariable long commentId,
			@RequestBody CommentRequest commentRequest) {
		return new DataResponse(HttpStatus.OK.value(), commentService.updateComment(userId, commentId, commentRequest),
				"Update successful");
	}

	@GetMapping("/tasks/{taskId}/comments")
	public PaginationResponse<CommentResponse> getcomments(@PathVariable String taskId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

		Direction direction = Direction.fromString(Direction.DESC.toString());
		Page<CommentResponse> comments = commentService.getCommentsofTask(taskId,
				PageRequest.of(page, size, Sort.by(direction, "createdAt")));

		List<CommentResponse> dataResponses = comments.getContent().stream().toList();

		PaginationResponse<CommentResponse> paginationResponse = new PaginationResponse<>(HttpStatus.OK, dataResponses,
				comments.getPageable().getPageNumber(), comments.getSize(), comments.getTotalElements(),
				comments.getTotalPages(), comments.getSort().isSorted(), comments.getSort().isUnsorted(),
				comments.getSort().isEmpty());

		return paginationResponse;
	}

	@DeleteMapping("/users/{userId}/comments/{commentId}")
	public DataResponse deletecomment(@PathVariable String userId, @PathVariable long commentId) {
		commentService.deleteComment(commentId, userId);
		return new DataResponse(HttpStatus.OK.value(), null, "Delete Successfull");
	}

}
