package com.quocbao.taskmanagementsystem.events.Mention;

import lombok.Getter;

@Getter
public class MentionUpdateEvent {
    private final Long userId;
    private final Long commentId;
    private final Long taskId;
    private final String textOld;
    private final String textNew;

    public MentionUpdateEvent(Long userId, Long commentId, Long taskId, String textOld, String textNew) {
        this.userId = userId;
        this.commentId = commentId;
        this.taskId = taskId;
        this.textOld = textOld;
        this.textNew = textNew;
    }
}
