package com.quocbao.taskmanagementsystem.service.utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.repository.ReportRepository;

@Service
public class ReportHelperService {

	private final ReportRepository reportRepository;
	
	public ReportHelperService(ReportRepository reportRepository) { 
		this.reportRepository = reportRepository;
	}
	
	public Map<Long, Long> countReport(List<Long> taskIds) {
		return reportRepository.countByTaskIds(taskIds).stream().collect(Collectors.toMap(
				ReportRepository.ReportCountProjection::getTaskId, ReportRepository.ReportCountProjection::getCount));
	}
}
