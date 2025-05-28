package com.quocbao.taskmanagementsystem.events;

import com.quocbao.taskmanagementsystem.service.MessageServiceBundle;
import com.quocbao.taskmanagementsystem.service.utils.UserHelperService;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.NotificationType;
import com.quocbao.taskmanagementsystem.entity.Notification;
import com.quocbao.taskmanagementsystem.firebase.FCMService;
import com.quocbao.taskmanagementsystem.payload.response.NotifiResponse;
import com.quocbao.taskmanagementsystem.repository.NotificationRepository;

@Component
public class NotificationEventHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEventHandler.class);

	private final MessageServiceBundle messageServiceBundle = new MessageServiceBundle();

	private final NotificationRepository notificationRepository;

	private final UserHelperService userHelperService;

	private final FCMService fcmService;

	private final IdEncoder idEncoder;

	public NotificationEventHandler(NotificationRepository notificationRepository, UserHelperService userHelperService,
			FCMService fcmService, IdEncoder idEncoder) {
		this.notificationRepository = notificationRepository;
		this.userHelperService = userHelperService;
		this.fcmService = fcmService;
		this.idEncoder = idEncoder;
	}

	String titleNoti;
	String body;

	@Async("eventTaskExecutor")
	@EventListener
	public void handlerTaskEvent(TaskEvent event) {
		processNotification(event.getReceiverId(),
				event.getContentType().equals(NotificationType.DUEAT.toString()) ? null : event.getSenderId(),
				event.getTaskId(), NotificationType.TASK.toString(), event.getContentType(), event.getSenderName(),
				event.getTaskTitle());
	}

	@Async("eventTaskExecutor")
	@EventListener
	public void handlerContactEvent(ContactEvent event) {
		processNotification(event.getReceiverId(), event.getSenderId(), event.getContactId(),
				NotificationType.CONTACT.toString(), NotificationType.CONTACT.toString(), event.getSenderName(), null);
	}

	@Async("eventTaskExecutor")
	@EventListener
	public void handlerCommentEvent(CommentEvent event) {
		processNotification(event.getReceiverId(), event.getSenderId(), event.getContentId(),
				NotificationType.COMMENT.toString(), NotificationType.COMMENT.toString(), event.getSenderName(),
				event.getTitleTask());
	}

	@Async("eventTaskExecutor")
	@EventListener
	public void handlerReportEvent(ReportEvent event) {
		processNotification(event.getReceiverId(), event.getSenderId(), event.getTaskId(),
				NotificationType.TASK.toString(), event.getContentType(), event.getSenderName(), event.getTaskTitle());
	}

	private void processNotification(Long receiverId, Long senderId, Long contentId, String type, String typeContent,
			String senderName, String title) {

		Notification notification = Notification.builder().receiverId(receiverId).senderId(senderId)
				.contentId(contentId).type(type).typeContent(typeContent).build();
		notificationRepository.save(notification);

		Optional.ofNullable(userHelperService.userExist(idEncoder.endcode(receiverId))).ifPresent(user -> {
			titleNoti = senderName + " " + titleNoti(user.get().getLanguage(), type, typeContent) + " " + title;
			if (senderName == null) {
				titleNoti = title + " " + titleNoti(user.get().getLanguage(), type, typeContent);
			}
			body = messageServiceBundle.getMessage("notification.body", user.get().getLanguage());
			sendPushNotification(notification, senderName, title, user.get().getToken(), titleNoti, body);
		});
	}

	public void sendPushNotification(Notification notification, String senderName, String title, String token,
			String titleNoti, String body) {
		try {
			fcmService.sendMessage(buildPayload(notification, senderName, title), token, notification.getType(),
					titleNoti, body);
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error("Failed to send push notification", e);
		}
	}

	private Map<String, String> buildPayload(Notification notification, String senderName, String title) {
		NotifiResponse notifiResponse = new NotifiResponse(notification.getId(), null,
				idEncoder.endcode(notification.getContentId()), senderName != null ? senderName : "", null,
				notification.getTypeContent(), title != null ? title : "", false, notification.getType(), null);
		return notifiResponse.toMap();
	}

	private String titleNoti(String language, String type, String typeContent) {
		if (type.equals(NotificationType.TASK.toString())) {
			if (typeContent.equals(NotificationType.NEW_ASSIGN.toString())) {
				return messageServiceBundle.getMessage("notification.task.title.assign", language);
			} else if (typeContent.equals(NotificationType.REMOVE_ASSIGN.toString())) {
				return messageServiceBundle.getMessage("notification.task.title.reassign", language);
			} else if (typeContent.equals(NotificationType.REPORT.toString())) {
				return messageServiceBundle.getMessage("notification.task.report", language);
			} else {
				return messageServiceBundle.getMessage("notification.task.title.dueat", language);
			}
		} else if (type.equals(NotificationType.COMMENT.toString())) {
			return messageServiceBundle.getMessage("notification.comment.title", language);
		} else {
			return messageServiceBundle.getMessage("notification.contact.title", language);
		}
	}
}
