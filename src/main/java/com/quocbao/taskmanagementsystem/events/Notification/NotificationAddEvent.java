package com.quocbao.taskmanagementsystem.events.Notification;

import lombok.Getter;

@Getter
public class NotificationAddEvent {

    private final Long senderId;
    private final Long receiverId;
    private final Long contentId;
    private final String notificationType;
    private final String contentType;

    public NotificationAddEvent(Long senderId, Long receiverId, Long contentId, String notificationType,
            String contentType) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.contentId = contentId;
        this.notificationType = notificationType;
        this.contentType = contentType;
    }
}
