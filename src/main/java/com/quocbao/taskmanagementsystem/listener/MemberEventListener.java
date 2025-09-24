package com.quocbao.taskmanagementsystem.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.quocbao.taskmanagementsystem.events.Member.AddMemberEvent;
import com.quocbao.taskmanagementsystem.service.TeamMemberService;

@Component
public class MemberEventListener {

    private final TeamMemberService teamMemberService;

    public MemberEventListener(TeamMemberService teamMemberService) {
        this.teamMemberService = teamMemberService;
    }

    @Async("event_notifi")
    @EventListener
    public void AddLeaderEvent(AddMemberEvent addMemberEvent) {
        teamMemberService.addLeaderTeam(addMemberEvent.getUserId(), addMemberEvent.getTeamId());
    }
}
