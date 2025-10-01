package com.quocbao.taskmanagementsystem.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.quocbao.taskmanagementsystem.entity.Notification;
import com.quocbao.taskmanagementsystem.events.Notification.NotificationAddEvent;
import com.quocbao.taskmanagementsystem.service.NotificationService;

@Component
public class NotificationEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Async("event_notifi")
    @EventListener
    public void handlerNotificationAddEvent(NotificationAddEvent event) {
        LOGGER.info("Running in: " + Thread.currentThread().getName());
        Notification notification = Notification.builder().senderId(event.getSenderId())
                .receiverId(event.getReceiverId()).contentId(event.getContentId()).type(event.getNotificationType())
                .typeContent(event.getContentType())
                .build();
        notificationService.saveNotification(notification);
    }
}
