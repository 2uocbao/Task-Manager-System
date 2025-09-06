package com.quocbao.taskmanagementsystem.common;

import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Setter;

@Setter
public class PaginationResponse<T> {

	@JsonProperty("status")
	private int status;

	@JsonProperty("data")
	private List<T> data;

	@JsonProperty("pagination")
	private Pagination pagination;

	@JsonProperty("sort")
	private Sort sort;

	public PaginationResponse(HttpStatus httpStatus, List<T> data, int currentPage, int perPage, Long totalItems,
			int totalPages, boolean sorted, boolean unsorted, boolean empty) {
		this.status = httpStatus.value();
		this.data = data;
		this.pagination = new Pagination(currentPage, perPage, totalItems, totalPages);
		this.sort = new Sort(sorted, unsorted, empty);
	}
	
	public PaginationResponse(HttpStatus httpStatus, List<T> data) {
		this.status = httpStatus.value();
		this.data = data;
	}

	@Setter
	public class Pagination {

		@JsonProperty("current_page")
		private int currentPage;

		@JsonProperty("per_page")
		private int perPage;

		@JsonProperty("total_items")
		private Long totalItems;

		@JsonProperty("total_pages")
		private int totalPages;

		public Pagination(int currentPage, int perPage, Long totalItems, int totalPages) {
			this.currentPage = currentPage;
			this.perPage = perPage;
			this.totalPages = totalPages;
			this.totalItems = totalItems;
		}
	}

	public class Sort {
		@JsonProperty("sorted")
		private boolean sorted;

		@JsonProperty("unsorted")
		private boolean unsorted;

		@JsonProperty("empty")
		private boolean empty;

		public Sort(boolean sorted, boolean unsorted, boolean empty) {
			this.sorted = sorted;
			this.unsorted = unsorted;
			this.empty = empty;
		}
	}
}