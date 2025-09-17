package com.quocbao.taskmanagementsystem.events.Mention;

import java.util.List;

import lombok.Getter;

@Getter
public class MentionUpdateEvent {

    private final Long sender;
    private final Long commentId;
    private final List<String> userId;

    public MentionUpdateEvent(Long sender, Long commentId, List<String> userId) {
        this.sender = sender;
        this.commentId = commentId;
        this.userId = userId;
    }
}
