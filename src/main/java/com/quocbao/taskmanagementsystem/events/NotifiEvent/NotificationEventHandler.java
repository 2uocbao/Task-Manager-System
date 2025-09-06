package com.quocbao.taskmanagementsystem.events.NotifiEvent;

import com.quocbao.taskmanagementsystem.service.MessageServiceBundle;
import com.quocbao.taskmanagementsystem.service.NotificationService;

import java.util.Map;
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

@Component
public class NotificationEventHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEventHandler.class);

	private final MessageServiceBundle messageServiceBundle = new MessageServiceBundle();

	private final NotificationService notificationService;

	private final FCMService fcmService;

	private final IdEncoder idEncoder;

	public NotificationEventHandler(NotificationService notificationService, FCMService fcmService,
			IdEncoder idEncoder) {
		this.notificationService = notificationService;
		this.fcmService = fcmService;
		this.idEncoder = idEncoder;
	}

	String titleNoti;
	String body;

	@Async("event_notifi")
	@EventListener
	public void handlerTaskEvent(TaskEvent event) {
		processNotification(event.getReceiverId(),
				event.getContentType().equals(NotificationType.DUEAT.toString()) ? null : event.getSenderId(),
				event.getTaskId(), NotificationType.TASK.toString(), event.getContentType(), event.getSenderName(),
				event.getTaskTitle(), null, event.getLanguage(), event.getToken());
	}

	@Async("event_notifi")
	@EventListener
	public void handlerContactEvent(ContactEvent event) {
		processNotification(event.getReceiverId(), event.getSenderId(), event.getContactId(),
				NotificationType.CONTACT.toString(), event.getContentType(), event.getSenderName(), null, null,
				event.getLanguage(), event.getToken());
	}

	@Async("event_notifi")
	@EventListener
	public void handlerCommentEvent(CommentEvent event) {
		processNotification(event.getReceiverId(), event.getSenderId(), event.getContentId(),
				NotificationType.COMMENT.toString(), NotificationType.COMMENT.toString(), event.getSenderName(),
				event.getTitleTask(), null, event.getLanguage(), event.getToken());
	}

	@Async("event_notifi")
	@EventListener
	public void handlerReportEvent(ReportEvent event) {
		processNotification(event.getReceiverId(), event.getSenderId(), event.getTaskId(),
				NotificationType.TASK.toString(), event.getContentType(), event.getSenderName(), event.getTaskTitle(),
				null,
				event.getLanguage(), event.getToken());
	}

	@Async("event_notifi")
	@EventListener
	public void handlerMemberEvent(TeamMemberEvent teamMemberEvent) {
		processNotification(teamMemberEvent.getReceiverId(), teamMemberEvent.getSenderId(),
				teamMemberEvent.getContentId(), NotificationType.TEAM.toString(), teamMemberEvent.getContentType(),
				teamMemberEvent.getSenderName(), null, teamMemberEvent.getTeamName(), teamMemberEvent.getLanguage(),
				teamMemberEvent.getToken());
	}

	private void processNotification(Long receiverId, Long senderId, Long contentId, String type, String typeContent,
			String senderName, String title, String nameTeam, String language, String token) {

		try {
			Notification notification = Notification.builder().receiverId(receiverId).senderId(senderId)
					.contentId(contentId).type(type).typeContent(typeContent).build();

			notificationService.saveNotification(notification);

			if (type.equals(NotificationType.TEAM.toString())) {
				title = nameTeam;
			}

			titleNoti = senderName + " " + titleNoti(language, type, typeContent) + " " + title;
			if (senderName == null) {
				titleNoti = title + " " + titleNoti(language, type, typeContent);
			}
			body = messageServiceBundle.getMessage("notification.body", language);
			if (token != null) {
				sendPushNotification(notification, senderName, title, nameTeam, token, titleNoti, body);
			}

		} catch (Exception e) {
			LOGGER.error("Error sending notification to user {}: {}", receiverId, e.getMessage());
		}

	}

	public void sendPushNotification(Notification notification, String senderName, String title, String nameTeam,
			String token,
			String titleNoti, String body) {
		try {
			fcmService.sendMessage(buildPayload(notification, senderName, title, nameTeam), token,
					notification.getType(),
					titleNoti, body);
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error("Failed to send push notification", e);
		}
	}

	private Map<String, String> buildPayload(Notification notification, String senderName, String title,
			String teamName) {
		NotifiResponse notifiResponse = new NotifiResponse(notification.getId(), null,
				idEncoder.encode(notification.getContentId()), senderName != null ? senderName : "", null,
				notification.getTypeContent(), title != null ? title : "", teamName != null ? teamName : "", false,
				notification.getType(), null);
		return notifiResponse.toMap();
	}

	private String titleNoti(String language, String type, String typeContent) {
		switch (type) {
			case "TASK":
				switch (typeContent) {
					case "NEW_ASSIGN":
						return messageServiceBundle.getMessage("notification.task.title.assign", language);
					case "REMOVE_ASSIGN":
						return messageServiceBundle.getMessage("notification.task.title.reassign", language);
					case "REPORT":
						return messageServiceBundle.getMessage("notification.task.report", language);
					case "DUEAT":
						return messageServiceBundle.getMessage("notification.task.title.dueat", language);
					default:
						break;
				}
			case "COMMENT":
				return messageServiceBundle.getMessage("notification.comment.title", language);
			case "CONTACT":
				switch (typeContent) {
					case "CONTACT":
						return messageServiceBundle.getMessage("notification.contact.title", language);
					case "CONTACTACEPT":
						return messageServiceBundle.getMessage("notification.contact.title.accept", language);
					default:
						break;
				}
			case "TEAM":
				switch (typeContent) {
					case "ADD_MEMBER":
						return messageServiceBundle.getMessage("notification.team.title.add", language);
					case "REMOVE_MEMBER":
						return messageServiceBundle.getMessage("notification.team.title.remove", language);
					case "LEAVE_MEMBER":
						return messageServiceBundle.getMessage("notification.team.title.leave", language);
					default:
						break;
				}
			default:
				return "";
		}
	}
}
