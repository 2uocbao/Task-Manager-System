package com.quocbao.taskmanagementsystem.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.quocbao.taskmanagementsystem.events.Mention.MentionAddEvent;
import com.quocbao.taskmanagementsystem.events.Mention.MentionUpdateEvent;
import com.quocbao.taskmanagementsystem.service.MentionService;

@Component
public class MentionEventListener {

    private final MentionService mentionService;

    public MentionEventListener(
            MentionService mentionService) {
        this.mentionService = mentionService;
    }

    @Async("event_notifi")
    @EventListener
    public void addMention(MentionAddEvent addMentionEvent) {
        mentionService.createMention(addMentionEvent.getUserId(),
                addMentionEvent.getCommentId(),
                addMentionEvent.getMentionId());
    }

    @Async("event_notifi")
    @EventListener
    public void updateMentionByCommentId(MentionUpdateEvent mentionUpdateEvent) {
        mentionService.updateMention(mentionUpdateEvent.getSender(), mentionUpdateEvent.getCommentId(),
                mentionUpdateEvent.getUserId());
    }
}
