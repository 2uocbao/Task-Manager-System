package com.quocbao.taskmanagementsystem.events.Member;

import lombok.Getter;

@Getter
public class AddMemberEvent {

    private Long userId;
    private Long teamId;

    public AddMemberEvent(Long userId, Long teamId) {
        this.userId = userId;
        this.teamId = teamId;
    }
}
