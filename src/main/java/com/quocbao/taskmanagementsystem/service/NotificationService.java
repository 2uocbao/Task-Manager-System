package com.quocbao.taskmanagementsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.quocbao.taskmanagementsystem.payload.request.NotifiRequest;
import com.quocbao.taskmanagementsystem.payload.response.NotifiResponse;

public interface NotificationService {

	public void createNotification(NotifiRequest notifiRequest);

	public void updateStatusNotification(String userId, Long notificationId);
	
	public void updateStatusAll(String userId);
	
	public void deleteNotification(Long receiverId, Long senderId,  String type);

	Page<NotifiResponse> getNotifications(String userId, String type, Boolean status, Pageable pageable);
}
