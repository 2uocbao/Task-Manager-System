package com.quocbao.taskmanagementsystem.events.Mention;

import lombok.Getter;

@Getter
public class MentionAddEvent {

    private final Long userId;
    private final Long commentId;
    private final Long taskId;
    private final String text;

    public MentionAddEvent(Long userId, Long commentId, Long taskId, String text) {
        this.userId = userId;
        this.commentId = commentId;
        this.taskId = taskId;
        this.text = text;
    }
}
