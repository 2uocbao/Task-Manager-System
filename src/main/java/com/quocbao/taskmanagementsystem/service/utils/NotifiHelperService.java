package com.quocbao.taskmanagementsystem.service.utils;

import java.util.List;
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
		notificationRepository.deleteByContentIdAndType(contentId, type);
	}
	
	public void deleteByContentIdsAndType(List<Long> contentIds, String type) {
		notificationRepository.deleteByContentIdsAndType(contentIds, type);
	}

	public void updateNotifi(Long contentId, String type) {
		Optional.ofNullable(notificationRepository.findByContentIdAndType(contentId, type)).ifPresent(notifi -> {
			notifi.setTypeContent(NotificationType.CONTACTACEPT.toString());
			notificationRepository.save(notifi);
		});
	}
}
