package com.quocbao.taskmanagementsystem.serviceimpl;

import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.entity.Notification;
import com.quocbao.taskmanagementsystem.events.DeleteEvent.NotifiDeletedEvent;
import com.quocbao.taskmanagementsystem.events.DeleteEvent.NotifiLeavedEvent;
import com.quocbao.taskmanagementsystem.payload.response.NotifiResponse;
import com.quocbao.taskmanagementsystem.repository.NotificationRepository;
import com.quocbao.taskmanagementsystem.service.NotificationService;
import com.quocbao.taskmanagementsystem.service.utils.AuthenticationService;
import com.quocbao.taskmanagementsystem.specifications.NotificationSpecification;

@Service
public class NotificationServiceImpl implements NotificationService {

	private final NotificationRepository notificationRepository;

	private final AuthenticationService authService;

	private final IdEncoder idEncoder;

	private NotificationServiceImpl(NotificationRepository notificationRepository, AuthenticationService authService,
			IdEncoder idEncoder) {
		this.notificationRepository = notificationRepository;
		this.authService = authService;
		this.idEncoder = idEncoder;
	}

	@Override
	public void saveNotification(Notification notification) {
		notificationRepository.save(notification);
	}

	@Override
	public void updateStatusNotification(Long notificationId) {
		notificationRepository.findById(notificationId).ifPresent(notification -> {
			notification.setRead(true);
			notificationRepository.save(notification);
		});
	}

	@Override
	public void updateStatusAll() {
		Long currentUserId = authService.getUserIdInContext();
		List<Notification> notifications = notificationRepository.findAllByReceiverIdAndIsReadFalse(currentUserId);
		for (Notification n : notifications) {
			n.setRead(true);
		}
		notificationRepository.saveAll(notifications);
	}

	@Override
	public Page<NotifiResponse> getNotifications(String type, Boolean status, Pageable pageable) {
		Long currentUserId = authService.getUserIdInContext();
		return notificationRepository.getNotifications(currentUserId, type, status, pageable)
				.map(notifi -> new NotifiResponse(notifi.getId(),
						notifi.getSenderId() != null ? idEncoder.encode(notifi.getSenderId()) : null,
						idEncoder.encode(notifi.getContentId()),
						(notifi.getFirstName() != null ? notifi.getFirstName() : "")
								+ (notifi.getLastName() != null ? " " + notifi.getLastName() : ""),
						notifi.getImage() != null ? notifi.getImage() : "", notifi.getTypeContent(),
						notifi.getTitle() != null ? notifi.getTitle() : "",
						notifi.getName() != null ? notifi.getName() : "", notifi.getIsRead(), notifi.getType(),
						notifi.getCreatedAt()));
	}

	@Override
	public boolean haveUnRead() {
		Long currentUserId = authService.getUserIdInContext();
		return notificationRepository.existsByReceiverIdAndIsReadIsFalse(currentUserId);
	}

	@EventListener
	public void deleteNotification(NotifiDeletedEvent event) {
		notificationRepository
				.delete(Specification.where(NotificationSpecification.getNotificationsByContentId(event.getContentId())
						.and(NotificationSpecification.getNotificationByType(event.getType()))));
	}

	@EventListener
	public void leavedNotification(NotifiLeavedEvent event) {
		notificationRepository
				.delete(Specification.where(NotificationSpecification.getNotificationsByReceiverId(event.getUserId())
						.and(NotificationSpecification.getNotificationByType(event.getType()))));
	}
}
