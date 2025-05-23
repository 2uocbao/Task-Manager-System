package com.quocbao.taskmanagementsystem.specifications;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.quocbao.taskmanagementsystem.entity.Comment;
import com.quocbao.taskmanagementsystem.entity.Comment_;
import com.quocbao.taskmanagementsystem.entity.Task_;

public class CommentSpecification {

	public CommentSpecification() {

	}

	public static Specification<Comment> findByTaskId(List<Long> taskId) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Comment_.task).get(Task_.id), taskId);
	}
	
	public static Specification<Comment> getCommentOfTask(Long taskId) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Comment_.task).get(Task_.id), taskId);
	}
}
