package com.quocbao.taskmanagementsystem.serviceimpl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.MethodGeneral;
import com.quocbao.taskmanagementsystem.entity.Notification;
import com.quocbao.taskmanagementsystem.firebase.FCMInitializer;
import com.quocbao.taskmanagementsystem.firebase.FCMService;
import com.quocbao.taskmanagementsystem.payload.request.NotifiRequest;
import com.quocbao.taskmanagementsystem.payload.response.NotifiResponse;
import com.quocbao.taskmanagementsystem.repository.NotificationRepository;
import com.quocbao.taskmanagementsystem.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

	@Value("#{${app.notifications.defaults}}")
	private Map<String, String> defaults;

	private final FCMService fcmService;

	private final NotificationRepository notificationRepository;

	private final IdEncoder idEncoder;

	private final MethodGeneral methodGeneral;

	private NotificationServiceImpl(NotificationRepository notificationRepository, FCMService fcmService,
			FCMInitializer FCMInitializer, IdEncoder idEncoder, MethodGeneral methodGeneral) {
		this.notificationRepository = notificationRepository;
		this.fcmService = fcmService;
		this.idEncoder = idEncoder;
		this.methodGeneral = methodGeneral;
	}

	@Override
	public void updateStatusNotification(String userId, Long notificationId) {
		notificationRepository.findById(notificationId).ifPresent(notification -> {
			methodGeneral.validatePermission(notification.getReceiverId(), idEncoder.decode(userId));
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
	public void deleteNotification(Long receiverId, Long senderId, String type) {
		notificationRepository.deleteByReceiverIdOrUserIdAndType(receiverId, senderId,
				type);
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
	public void createNotification(NotifiRequest notifiRequest) {
		saveNotification(notifiRequest);
		sendPushNotification(notifiRequest, notifiRequest.getTokenFcm());
	}

	public void sendPushNotification(NotifiRequest notifiRequest, String token) {
		try {
			fcmService.sendMessage(buildPayload(notifiRequest), token, notifiRequest.getType(),
					notifiRequest.getTypeContent(), notifiRequest.getContentId());
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error(e.getMessage());
		}
	}

	private Map<String, String> buildPayload(NotifiRequest notifiRequest) {
		NotifiResponse notifiResponse = new NotifiResponse(null,
				notifiRequest.getSenderId() != null ? notifiRequest.getSenderId() : null, notifiRequest.getContentId(),
				notifiRequest.getSenderName() != null ? notifiRequest.getSenderName() : "", null,
				notifiRequest.getTypeContent(),
				notifiRequest.getTitleTask() != null ? notifiRequest.getTitleTask() : "", false,
				notifiRequest.getType(), null);
		return notifiResponse.toMap();
	}

	private Notification saveNotification(NotifiRequest notifiRequest) {
		Notification notification = Notification.builder().receiverId(idEncoder.decode(notifiRequest.getReceiverId()))
				.typeContent(notifiRequest.getTypeContent()).type(notifiRequest.getType())
				.contentId(idEncoder.decode(notifiRequest.getContentId()))
				.senderId(idEncoder.decode(notifiRequest.getSenderId())).build();
		return notificationRepository.save(notification);
	}
}
