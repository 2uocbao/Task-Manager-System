package com.quocbao.taskmanagementsystem.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.quocbao.taskmanagementsystem.payload.request.ReportRequest;
import com.quocbao.taskmanagementsystem.payload.response.ReportResponse;

public interface ReportService {

	public ReportResponse createReportForFile(MultipartFile file, String userId, String taskId) throws IOException;

	public ReportResponse createReportForLink(ReportRequest reportRequest);

	public void deleteReport(String reportId, String taskId, String userId);

	public List<ReportResponse> getReports(String taskId);

	public Path getFile(String userId, String taskId, String reportId);
}
