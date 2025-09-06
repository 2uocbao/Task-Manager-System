package com.quocbao.taskmanagementsystem.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quocbao.taskmanagementsystem.common.DataResponse;
import com.quocbao.taskmanagementsystem.common.PaginationResponse;
import com.quocbao.taskmanagementsystem.payload.response.NotifiResponse;
import com.quocbao.taskmanagementsystem.service.NotificationService;

@RestController
@RequestMapping()
public class NotificationController {

	private NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping("/notifications")
	public PaginationResponse<NotifiResponse> getNotificationsByUserId(@RequestParam(defaultValue = "ALL") String type,
			@RequestParam(defaultValue = "false") Boolean status, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		Direction direction = Direction.fromString(Direction.DESC.toString());
		Page<NotifiResponse> notifications = notificationService.getNotifications(type, status,
				PageRequest.of(page, size, Sort.by(direction, "createdAt")));

		List<NotifiResponse> entityModels = notifications.getContent().stream().toList();

		PaginationResponse<NotifiResponse> paginationResponse = new PaginationResponse<>(HttpStatus.OK, entityModels,
				notifications.getPageable().getPageNumber(), notifications.getSize(), notifications.getTotalElements(),
				notifications.getTotalPages(), notifications.getSort().isSorted(), notifications.getSort().isUnsorted(),
				notifications.getSort().isEmpty());

		return paginationResponse;
	}

	@PutMapping("/notifications")
	public DataResponse updateStatusAll() {
		notificationService.updateStatusAll();
		return new DataResponse(HttpStatus.OK.value(), null, "Success");
	}

	@PutMapping("/notifications/{notificationId}")
	public DataResponse updateStatus(@PathVariable Long notificationId) {
		notificationService.updateStatusNotification(notificationId);
		return new DataResponse(HttpStatus.OK.value(), null, "Success");
	}

	@GetMapping("/notifications/unread")
	public DataResponse haveUnRead() {
		return new DataResponse(HttpStatus.OK.value(), notificationService.haveUnRead(), "Success");
	}
}
