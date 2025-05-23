package com.quocbao.taskmanagementsystem.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.quocbao.taskmanagementsystem.entity.Report;
import com.quocbao.taskmanagementsystem.entity.Report_;
import com.quocbao.taskmanagementsystem.entity.Task_;

public class ReportSpecification {
	private ReportSpecification() {

	}
	
	public static Specification<Report> getReportByTaskId(Long taskId) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Report_.task).get(Task_.id), taskId);
	}
	
	public static Specification<Report> getReportByType(String type) {
		return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Report_.type), type);
	}
}
