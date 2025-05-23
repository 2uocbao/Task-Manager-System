package com.quocbao.taskmanagementsystem.repository;

import java.sql.Timestamp;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quocbao.taskmanagementsystem.entity.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

	@Query("SELECT "
			+ "t.id AS id, "
			+ "t.title AS title, "
			+ "t.status AS status, "
			+ "t.dueAt AS dueAt, "
			+ "COUNT (DISTINCT c.id) AS commentCount, "
			+ "COUNT (DISTINCT r.id) AS reportCount "
			+ "FROM Task t "
			+ "LEFT JOIN Comment c ON c.task.id = t.id "
			+ "LEFT JOIN Report r ON r.task.id = t.id "
			+ "WHERE ((:type = true AND t.user.id = :userId OR t.assignTo.id = :userId) "
			+ "OR (:type = false AND t.user.id = :userId AND t.assignTo.id IS NOT NULL)) "
			+ "AND LOWER(t.title) LIKE LOWER(CONCAT('%', :keySearch, '%')) "
			+ "GROUP BY t.id, t.title, t.status")
	Page<TaskProjections> searchTask(@Param("userId") Long userId, @Param("keySearch") String keySearch, @Param("type") boolean type,  Pageable pageable);
	interface TaskProjections {
		Long getId();
		String getTitle();
		String getStatus();
		Timestamp getDueAt();
		Long getCommentCount();
		Long getReportCount();
	}
}
