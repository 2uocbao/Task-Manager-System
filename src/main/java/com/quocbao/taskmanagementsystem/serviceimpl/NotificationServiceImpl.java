package com.quocbao.taskmanagementsystem.serviceimpl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.entity.Notification;
import com.quocbao.taskmanagementsystem.payload.response.NotifiResponse;
import com.quocbao.taskmanagementsystem.repository.NotificationRepository;
import com.quocbao.taskmanagementsystem.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

	private final NotificationRepository notificationRepository;

	private final IdEncoder idEncoder;

	private NotificationServiceImpl(NotificationRepository notificationRepository, IdEncoder idEncoder) {
		this.notificationRepository = notificationRepository;
		this.idEncoder = idEncoder;
	}

	@Override
	public void updateStatusNotification(String userId, Long notificationId) {
		notificationRepository.findById(notificationId).ifPresent(notification -> {
			notification.setRead(true);
			notificationRepository.save(notification);
		});
	}

	@Override
	public void updateStatusAll(String userId) {
		List<Notification> notifications = notificationRepository
				.findAllByReceiverIdAndIsReadFalse(idEncoder.decode(userId));
		for (Notification n : notifications) {
			n.setRead(true);
		}
		notificationRepository.saveAll(notifications);
	}

	@Override
	public Page<NotifiResponse> getNotifications(String userId, String type, Boolean status, Pageable pageable) {
		return notificationRepository.getNotifications(idEncoder.decode(userId), type, status, pageable)
				.map(notifi -> new NotifiResponse(notifi.getId(),
						notifi.getSenderId() != null ? idEncoder.endcode(notifi.getSenderId()) : null,
						idEncoder.endcode(notifi.getContentId()),
						(notifi.getFirstName() != null ? notifi.getFirstName() : "")
								+ (notifi.getLastName() != null ? " " + notifi.getLastName() : ""),
						notifi.getImage() != null ? notifi.getImage() : "", notifi.getTypeContent(),
						notifi.getTitleTask() != null ? notifi.getTitleTask() : "", notifi.getIsRead(),
						notifi.getType(), notifi.getCreatedAt()));

	}

	@Override
	public boolean haveUnRead(String userId) {
		return notificationRepository.existsByReceiverIdAndIsReadIsFalse(idEncoder.decode(userId));
	}
}
