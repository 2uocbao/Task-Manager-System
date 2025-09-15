package com.quocbao.taskmanagementsystem.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quocbao.taskmanagementsystem.common.PriorityEnum;
import com.quocbao.taskmanagementsystem.common.StatusEnum;
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
			+ "LEFT JOIN TaskAssignment ta ON ta.task.id = t.id "
			+ "WHERE ta.user.id = :userId AND t.team.id = :teamId AND t.title LIKE LOWER(CONCAT('%', :keySearch, '%')) "
			+ "GROUP BY t.id, t.title, t.status")
	Page<TaskProjections> searchTask(@Param("teamId") Long teamId, @Param("userId") Long userId,
			@Param("keySearch") String keySearch,
			Pageable pageable);

	interface TaskProjections {
		Long getId();

		String getTitle();

		String getStatus();

		Timestamp getDueAt();

		Long getCommentCount();

		Long getReportCount();
	}

	@Query("SELECT "
			+ "t.id AS id, "
			+ "t.title AS title, "
			+ "t.priority AS priority, "
			+ "t.dueAt AS dueAt, "
			+ "COUNT (DISTINCT c.id) AS commentCount, "
			+ "COUNT (DISTINCT r.id) AS reportCount "
			+ "FROM Task t "
			+ "LEFT JOIN Comment c ON c.task.id = t.id "
			+ "LEFT JOIN Report r ON r.task.id = t.id "
			+ "WHERE t.id IN ( "
			+ "SELECT "
			+ "a.task.id "
			+ "FROM TaskAssignment a WHERE a.user.id = :userId"
			+ " ) "
			+ " AND (t.dueAt BETWEEN :startDate AND :endDate) AND "
			+ " t.team.id = :teamId AND t.status = :status AND t.priority = :priority "
			+ "GROUP BY t.id, t.title, t.status, t.dueAt")
	Page<TaskProjection> getTask(@Param("userId") Long userId, @Param("teamId") Long teamId,
			@Param("status") StatusEnum status, @Param("priority") PriorityEnum priority,
			@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate, Pageable pageable);

	public interface TaskProjection {
		Long getId();

		String getTitle();

		String getPriority();

		Timestamp getDueAt();

		Long getCommentCount();

		Long getReportCount();
	}

	@Query("SELECT "
			+ "t.status AS status, "
			+ "count(DISTINCT t.id) AS taskCount "
			+ "FROM Task t "
			+ "LEFT JOIN TaskAssignment ta ON ta.task.id = t.id "
			+ "WHERE (t.user.id = :userId OR ta.user.id = :userId) AND t.team.id = :teamId  "
			+ "GROUP BY t.status")
	List<TaskSummaryProjection> getTaskSummary(@Param("userId") Long userId, @Param("teamId") Long teamId);

	interface TaskSummaryProjection {
		String getStatus();

		Long getTaskCount();
	}

}
