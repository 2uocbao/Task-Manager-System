package com.quocbao.taskmanagementsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.quocbao.taskmanagementsystem.entity.Notification;
import com.quocbao.taskmanagementsystem.payload.response.NotifiResponse;

public interface NotificationService {

	public void saveNotification(Notification notification);

	public void updateStatusNotification(Long notificationId);

	public void updateStatusAll();

	Page<NotifiResponse> getNotifications(String type, Boolean status, Pageable pageable);

	public boolean haveUnRead();
}
