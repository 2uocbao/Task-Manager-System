package com.quocbao.taskmanagementsystem.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.quocbao.taskmanagementsystem.common.DataResponse;
import com.quocbao.taskmanagementsystem.common.PaginationResponse;
import com.quocbao.taskmanagementsystem.payload.request.ReportRequest;
import com.quocbao.taskmanagementsystem.payload.response.ReportResponse;
import com.quocbao.taskmanagementsystem.service.ReportService;

@RestController
@RequestMapping
public class ReportController {
	private final ReportService reportService;

	public ReportController(ReportService reportService) {
		this.reportService = reportService;
	}

	@PostMapping("/reports/url")
	public DataResponse addReportForURL(@RequestBody ReportRequest reportRequest) {
		return new DataResponse(HttpStatus.OK.value(), reportService.createReportForLink(reportRequest),
				"Upload report successful");
	}

	@PostMapping(path = "/reports", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public DataResponse addReportForFile(@RequestParam MultipartFile file, @RequestParam String userId,
			@RequestParam String taskId) throws IOException {
		return new DataResponse(HttpStatus.OK.value(), reportService.createReportForFile(file, taskId),
				"Upload report successful");
	}

	@GetMapping("/tasks/{taskId}/reports")
	public PaginationResponse<ReportResponse> getReports(@PathVariable String taskId) {
		List<ReportResponse> reportResponse = reportService.getReports(taskId);
		PaginationResponse<ReportResponse> paginationResponse = new PaginationResponse<>(HttpStatus.OK, reportResponse,
				0, 0, 0L, 0, true, false, reportResponse.isEmpty());
		return paginationResponse;
	}

	@DeleteMapping("/reports/{reportId}")
	public DataResponse deleteReport(@PathVariable String reportId) {
		reportService.deleteReport(reportId);
		return new DataResponse(HttpStatus.OK.value(), null, "Delete report successful");
	}

	@GetMapping("/tasks/{taskId}/reports/{reportId}/download")
	public ResponseEntity<Resource> downloadFile(@PathVariable String taskId, @PathVariable String reportId)
			throws MalformedURLException {
		Path filePath = reportService.getFile(taskId, reportId).normalize();
		if (!Files.exists(filePath)) {
			return ResponseEntity.notFound().build();
		}
		Resource resource = new UrlResource(filePath.toUri());

		return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}

	@GetMapping("/tasks/{taskId}/reports/{reportId}/view")
	public ResponseEntity<Resource> viewImage(@PathVariable String taskId, @PathVariable String reportId)
			throws MalformedURLException {
		Path filePath = reportService.getFile(taskId, reportId).normalize();
		if (!Files.exists(filePath)) {
			return ResponseEntity.notFound().build();
		}
		Resource resource = new UrlResource(filePath.toUri());
		if (resource.exists() && resource.isReadable()) {
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "image/jpeg") // hoặc image/png
					.body(resource);
		} else {
			return ResponseEntity.notFound().build();
		}
	}
}
