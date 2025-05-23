package com.quocbao.taskmanagementsystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quocbao.taskmanagementsystem.entity.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {

	@Query("SELECT r.task.id as taskId, COUNT(r.id) AS count "
			+ "FROM Report r where r.task.id IN :taskIds GROUP BY r.task.id")
	List<ReportCountProjection> countByTaskIds(@Param("taskIds") List<Long> taskIds);

	interface ReportCountProjection {
		Long getTaskId();

		Long getCount();
	}
}
