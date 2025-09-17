package com.quocbao.taskmanagementsystem.events.Mention;

import java.util.List;

import lombok.Getter;

@Getter
public class MentionAddEvent {

    private final Long userId;
    private final Long commentId;
    private final List<String> mentionId;

    public MentionAddEvent(Long userId, Long commentId, List<String> mentionId) {
        this.userId = userId;
        this.commentId = commentId;
        this.mentionId = mentionId;
    }
}
