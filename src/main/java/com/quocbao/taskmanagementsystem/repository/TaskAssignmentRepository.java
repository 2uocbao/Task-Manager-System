package com.quocbao.taskmanagementsystem.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quocbao.taskmanagementsystem.entity.TaskAssignment;

@Repository
public interface TaskAssignmentRepository
		extends JpaRepository<TaskAssignment, Long>, JpaSpecificationExecutor<TaskAssignment> {

	@Query("SELECT "
			+ "ta.id AS id, "
			+ "u.id AS assignerId, "
			+ "u.firstName AS firstName, "
			+ "u.lastName AS lastName, "
			+ "u.mention AS mention, "
			+ "u.image AS image, "
			+ "ta.role AS role, "
			+ "ta.joinedAt AS joinedAt "
			+ "FROM TaskAssignment ta "
			+ "LEFT JOIN User u ON u.id = ta.user.id "
			+ "WHERE ta.task.id = :taskId")
	public List<TaskAssignmentProjection> getTaskAssignments(@Param("taskId") Long taskId);

	interface TaskAssignmentProjection {
		Long getId();

		Long getAssignerId();

		String getFirstName();

		String getLastName();

		String getMention();

		String getImage();

		String getRole();

		Timestamp getJoinedAt();
	}
}
