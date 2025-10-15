package com.quocbao.taskmanagementsystem.serviceimpl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.NotificationType;
import com.quocbao.taskmanagementsystem.entity.Notification;
import com.quocbao.taskmanagementsystem.entity.Task;
import com.quocbao.taskmanagementsystem.entity.Team;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.firebase.FCMService;
import com.quocbao.taskmanagementsystem.payload.response.NotifiResponse;
import com.quocbao.taskmanagementsystem.repository.NotificationRepository;
import com.quocbao.taskmanagementsystem.service.MessageServiceBundle;
import com.quocbao.taskmanagementsystem.service.NotificationService;
import com.quocbao.taskmanagementsystem.service.utils.AuthenticationService;
import com.quocbao.taskmanagementsystem.service.utils.TaskHelperService;
import com.quocbao.taskmanagementsystem.service.utils.TeamHelperService;
import com.quocbao.taskmanagementsystem.service.utils.UserHelperService;

@Service
public class NotificationServiceImpl implements NotificationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

	private final NotificationRepository notificationRepository;

	private final UserHelperService userHelperService;

	private final TeamHelperService teamHelperService;

	private final TaskHelperService taskHelperService;

	private final AuthenticationService authService;

	private final MessageServiceBundle messageServiceBundle = new MessageServiceBundle();

	private final FCMService fcmService;

	private final IdEncoder idEncoder;

	private NotificationServiceImpl(NotificationRepository notificationRepository, AuthenticationService authService,
			UserHelperService userHelperService, TeamHelperService teamHelperService,
			TaskHelperService taskHelperService,
			FCMService fcmService,
			IdEncoder idEncoder) {
		this.notificationRepository = notificationRepository;
		this.authService = authService;
		this.userHelperService = userHelperService;
		this.teamHelperService = teamHelperService;
		this.taskHelperService = taskHelperService;
		this.fcmService = fcmService;
		this.idEncoder = idEncoder;
	}

	@Override
	public void saveNotification(Notification notification) {
		Notification result = notificationRepository.save(notification);
		userHelperService.getUser(result.getReceiverId()).ifPresent((user -> {
			if (user.getToken() != null) {
				User sender = new User();
				if(notification.getTypeContent().equals(NotificationType.DUEAT.toString())) {
					 sender = userHelperService.getUser(result.getSenderId()).get();
				}
				switch (result.getType()) {
					case "TASK":
						Task task = taskHelperService.getTask(result.getContentId()).get();
						switch (result.getTypeContent()) {
							case "DUEAT":
								publishNotification(result, null,
										task.getTitle(),
										user.getLanguage(), user.getToken());
								LOGGER.info("Push notification due at task");
								break;
							default:
								publishNotification(result, sender.getFirstName() + sender.getLastName(),
										task.getTitle(),
										user.getLanguage(), user.getToken());
								LOGGER.info("Push notification about task");
								break;
						}
						break;
					case "COMMENT":
						Task task1 = taskHelperService.getTask(result.getContentId()).get();
						publishNotification(result, sender.getFirstName() + sender.getLastName(),
								task1.getTitle(),
								user.getLanguage(), user.getToken());
						LOGGER.info("Push notification mention comment");
						break;
					case "CONTACT":
						publishNotification(result, sender.getFirstName() + sender.getLastName(), "",
								user.getLanguage(), user.getToken());
						LOGGER.info("Push notification request contact");
						break;
					case "TEAM":
						Team team = teamHelperService.getTeamById(result.getContentId());
						publishNotification(result, sender.getFirstName() + sender.getLastName(), team.getName(),
								user.getLanguage(), user.getToken());
						LOGGER.info("Push notification about team");
						break;
					default:
						return;
				}
			}
		}));
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
						notifi.getTitle() != null ? notifi.getTitle() : notifi.getName(), notifi.getIsRead(),
						notifi.getType(),
						notifi.getCreatedAt()));
	}

	@Override
	public boolean haveUnRead() {
		Long currentUserId = authService.getUserIdInContext();
		return notificationRepository.existsByReceiverIdAndIsReadIsFalse(currentUserId);
	}

	private void publishNotification(Notification notification,
			String senderName, String subtitle, String language, String token) {

		try {
			String titleNoti = senderName + " "
					+ titleNoti(language, notification.getType(), notification.getTypeContent()) + " "
					+ subtitle;
			if (senderName == null) {
				titleNoti = subtitle + " " + titleNoti(language, notification.getType(), notification.getTypeContent());
			}
			String body = messageServiceBundle.getMessage("notification.body", language);
			if (token != null) {
				sendPushNotification(notification, senderName, subtitle, token, titleNoti, body);
			}

		} catch (Exception e) {
			LOGGER.error("Error sending notification to user {}: {}", e.getMessage());
		}

	}

	public void sendPushNotification(Notification notification, String senderName, String title,
			String token,
			String titleNoti, String body) {
		try {
			fcmService.sendMessage(buildPayload(notification, senderName, title), token,
					notification.getType(),
					titleNoti, body);
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error("Failed to send push notification", e);
		}
	}

	private Map<String, String> buildPayload(Notification notification, String senderName, String title) {
		NotifiResponse notifiResponse = new NotifiResponse(notification.getId(), null,
				idEncoder.encode(notification.getContentId()), senderName != null ? senderName : "", null,
				notification.getTypeContent(), title != null ? title : "", false,
				notification.getType(), notification.getCreatedAt());
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
