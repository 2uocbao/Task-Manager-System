package com.quocbao.taskmanagementsystem.service.utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.entity.Comment;
import com.quocbao.taskmanagementsystem.repository.CommentRepository;

@Service
public class CommentHelperService {

	private CommentRepository commentRepository;

	public CommentHelperService(CommentRepository commentRepository) {
		this.commentRepository = commentRepository;
	}

	public Comment getCommentById(Long commentId) {
		return commentRepository.findById(commentId).get();
	}

	public Map<Long, Long> countComment(List<Long> taskIds) {
		return commentRepository.countByTaskIds(taskIds).stream()
				.collect(Collectors.toMap(CommentRepository.CommentCountProjection::getTaskId,
						CommentRepository.CommentCountProjection::getCount));
	}
}
