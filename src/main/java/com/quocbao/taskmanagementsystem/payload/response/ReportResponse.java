package com.quocbao.taskmanagementsystem.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quocbao.taskmanagementsystem.common.ConvertData;
import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.entity.Report;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReportResponse {

	@JsonProperty("id")
	private String id;

	@JsonProperty("type")
	private String type;

	@JsonProperty("file_name")
	private String fileName;

	@JsonProperty("file_path")
	private String filePath;

	@JsonProperty("external_url")
	private String externalUrl;
	
	@JsonProperty("created_at")
	private String createdAt;

	public ReportResponse(Report report) {
		this.id = new IdEncoder().endcode(report.getId());
		this.type = report.getType().toString();
		this.fileName = report.getFileName();
		this.filePath = report.getFilePath();
		this.externalUrl = report.getExternalUrl();
		this.createdAt = ConvertData.timeStampToString(report.getCreatedAt());
	}
}
