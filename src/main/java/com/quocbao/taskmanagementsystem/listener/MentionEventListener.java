package com.quocbao.taskmanagementsystem.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.quocbao.taskmanagementsystem.events.Mention.MentionAddEvent;
import com.quocbao.taskmanagementsystem.service.utils.MentionHelperService;

@Component
public class MentionEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MentionEventListener.class);

    private final MentionHelperService mentionHelperService;

    public MentionEventListener(
            MentionHelperService mentionHelperService) {
        this.mentionHelperService = mentionHelperService;
    }

    @Async("event_notifi")
    @EventListener
    public void addMention(MentionAddEvent addMentionEvent) {
        LOGGER.info("Running in: " + Thread.currentThread().getName());
        mentionHelperService.getMentionAndPushNotiEvent(addMentionEvent.getUserId(),
                addMentionEvent.getTaskId(),
                addMentionEvent.getText());
    }
}
