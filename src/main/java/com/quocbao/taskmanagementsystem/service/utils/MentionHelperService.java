package com.quocbao.taskmanagementsystem.service.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.NotificationType;
import com.quocbao.taskmanagementsystem.events.Notification.NotificationAddEvent;

@Component
public class MentionHelperService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MentionHelperService.class);

    private ApplicationEventPublisher applicationEventPublisher;

    private UserHelperService userHelperService;

    private TaskAssignmentHelperService taskAssignmentHelperService;

    private IdEncoder idEncoder;

    public MentionHelperService(ApplicationEventPublisher applicationEventPublisher,
            UserHelperService userHelperService, TaskAssignmentHelperService taskAssignmentHelperService,
            IdEncoder idEncoder) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.userHelperService = userHelperService;
        this.taskAssignmentHelperService = taskAssignmentHelperService;
        this.idEncoder = idEncoder;
    }

    public void getMentionAndPushNotiEvent(Long userId, Long taskId, String text) {
        Pattern pattern = Pattern.compile("@\\[(.*?)\\]\\((.*?)\\)");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String mentionIdRough = matcher.group(1);
            String mentionId = mentionIdRough.substring(2, mentionIdRough.length() - 2);
            Long mentionIdLong = idEncoder.decode(mentionId);
            if (!userHelperService.isUserExist(mentionIdLong)
                    || !taskAssignmentHelperService.isUserInTask(mentionIdLong, taskId)) {
                LOGGER.info("User id {} can not access", mentionId);
            } else {
                applicationEventPublisher.publishEvent(new NotificationAddEvent(userId, mentionIdLong, taskId,
                        NotificationType.TASK.toString(), NotificationType.COMMENT.toString()));
            }
        }
    }

    public void newMentionUpdate(String oldText, String newText, Long taskId, Long userId, Long commentId) {
        Pattern pattern = Pattern.compile("@\\[(.*?)\\]\\((.*?)\\)");
        Matcher matcherOld = pattern.matcher(oldText);
        Matcher matcherNew = pattern.matcher(newText);
        Set<String> oldMention = new HashSet<>();
        Set<String> newMention = new HashSet<>();
        while (matcherOld.find()) {
            String mentionIdRough = matcherOld.group(1);
            String mentionId = mentionIdRough.substring(2, mentionIdRough.length() - 2);
            oldMention.add(mentionId);
        }
        while (matcherNew.find()) {
            String mentionIdRough = matcherNew.group(1);
            String mentionId = mentionIdRough.substring(2, mentionIdRough.length() - 2);
            newMention.add(mentionId);
        }

        Set<String> difference = new HashSet<>(newMention);
        difference.removeAll(oldMention);
        difference.stream().forEach(mentionId -> {
            Long mentionIdLong = idEncoder.decode(mentionId);
            applicationEventPublisher.publishEvent(new NotificationAddEvent(userId, mentionIdLong, taskId,
                    NotificationType.TASK.toString(), NotificationType.COMMENT.toString()));
        });
    }
}
