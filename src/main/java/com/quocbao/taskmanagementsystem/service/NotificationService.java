package com.quocbao.taskmanagementsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.quocbao.taskmanagementsystem.payload.response.NotifiResponse;

public interface NotificationService {

	public void updateStatusNotification(String userId, Long notificationId);
	
	public void updateStatusAll(String userId);

	Page<NotifiResponse> getNotifications(String userId, String type, Boolean status, Pageable pageable);
}
