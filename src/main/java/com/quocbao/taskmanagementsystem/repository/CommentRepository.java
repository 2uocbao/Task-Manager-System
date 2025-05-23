package com.quocbao.taskmanagementsystem.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.quocbao.taskmanagementsystem.entity.Comment;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {
	
	interface CommentCountProjection {
		Long getTaskId();
		Long getCount();
	}
	@Query("SELECT c.task.id as taskId, count(c.id) as count " + 
	"FROM Comment c where c.task.id IN :taskIds GROUP BY c.task.id")
	List<CommentCountProjection> countByTaskIds(@Param("taskIds") List<Long> taskIds);
	
	interface CommentsProjection {
		Long getId();
		String getText();
		Long getuserId();
		String getfirstName();
		String getlastName();
		String getimagePath();
		Timestamp getCreatedAt();
	}
	@Query("SELECT "
			+ "c.id AS id, "
			+ "c.text AS text, "
			+ "u.id AS userId,"
			+ "u.firstName AS firstName, "
			+ "u.lastName AS lastName, "
			+ "u.image AS imagePath, "
			+ "c.createdAt as createdAt "
			+ "FROM Comment c "
			+ "LEFT JOIN User u ON c.user.id = u.id "
			+ "WHERE c.task.id = :taskId")
	Page<CommentsProjection> getCommentsByTaskIds(@Param("taskId") Long taskId, Pageable pageable);
	
	
}