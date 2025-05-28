package com.quocbao.taskmanagementsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.quocbao.taskmanagementsystem.payload.request.CommentRequest;
import com.quocbao.taskmanagementsystem.payload.response.CommentResponse;

public interface CommentService {

	public CommentResponse createComment(String userId, String taskId, CommentRequest commentRequest);

	public CommentResponse updateComment(String userId, long commentId, CommentRequest commentRequest);

	public Page<CommentResponse> getCommentsofTask(String taskId, Pageable pageable);

	public void deleteComment(long commentId, String userId);
}
