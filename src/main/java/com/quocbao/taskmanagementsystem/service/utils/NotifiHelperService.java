package com.quocbao.taskmanagementsystem.service.utils;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.common.NotificationType;
import com.quocbao.taskmanagementsystem.repository.NotificationRepository;

@Service
public class NotifiHelperService {

	private final NotificationRepository notificationRepository;

	public NotifiHelperService(NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	public void deleteNotification(Long contentId, String type) {
		notificationRepository.deleteByReceiverIdOrUserIdAndType(contentId, type);
	}

	public void updateNotifi(Long contentId, String type) {
		Optional.ofNullable(notificationRepository.findByContentIdAndType(contentId, type)).ifPresent(notifi -> {
			notifi.setTypeContent(NotificationType.CONTACTACEPT.toString());
			notificationRepository.save(notifi);
		});
	}
}
