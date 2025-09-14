package com.quocbao.taskmanagementsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.NotificationType;
import com.quocbao.taskmanagementsystem.entity.Team;
import com.quocbao.taskmanagementsystem.entity.TeamMember;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.events.Notification.NotificationAddEvent;
import com.quocbao.taskmanagementsystem.exception.AccessDeniedException;
import com.quocbao.taskmanagementsystem.exception.DuplicateException;
import com.quocbao.taskmanagementsystem.exception.ForbiddenException;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.TeamMemberRequest;
import com.quocbao.taskmanagementsystem.payload.response.TeamMemberResponse;
import com.quocbao.taskmanagementsystem.repository.TeamMemberRepository;
import com.quocbao.taskmanagementsystem.repository.TeamMemberRepository.TeamMemberProjection;
import com.quocbao.taskmanagementsystem.service.utils.AuthenticationService;
import com.quocbao.taskmanagementsystem.service.utils.ContactHelperService;
import com.quocbao.taskmanagementsystem.service.utils.TaskAssignmentHelperService;
import com.quocbao.taskmanagementsystem.serviceimpl.TeamMemberServiceImpl;

@ExtendWith(MockitoExtension.class)
public class TeamMemberTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private TeamMemberRepository memberRepository;

    @Mock
    private ContactHelperService contactService;

    @Mock
    private TaskAssignmentHelperService assignService;

    @Mock
    private AuthenticationService authService;

    @Mock
    private IdEncoder idEncoder;

    @InjectMocks
    private TeamMemberServiceImpl memberService;

    Long userId = 1L;
    String teamIdS = "teamId";
    String uMemberIdS = "userId";
    Long teamIdL = 1L;
    Long uMemberIdL = 2L;
    TeamMemberRequest teamMemberRequest = new TeamMemberRequest();

    @BeforeEach
    void defaultValue() {
        when(idEncoder.decode(teamIdS)).thenReturn(teamIdL);
        // when(idEncoder.decode(uMemberIdS)).thenReturn(uMemberIdL);
        when(authService.getUserIdInContext()).thenReturn(userId);
        teamMemberRequest.setMemberId(uMemberIdS);
    }

    @Test
    void testCreate_Success() {
        when(memberRepository.exists(any(Specification.class))).thenReturn(true).thenReturn(false);
        when(contactService.isConnected(anyLong(), anyLong())).thenReturn(true);

        TeamMember teamMember = TeamMember.builder().id(1L).user(User.builder().id(uMemberIdL).build())
                .team(Team.builder().id(teamIdL).build()).joinedAt(Timestamp.valueOf(LocalDateTime.now())).build();

        when(memberRepository.save(any(TeamMember.class))).thenReturn(teamMember);
        memberService.createTeamMember(teamIdS, teamMemberRequest);

        verify(memberRepository, times(1)).save(any(TeamMember.class));

        ArgumentCaptor<NotificationAddEvent> captor = ArgumentCaptor.forClass(NotificationAddEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        NotificationAddEvent event = captor.getValue();
        assertEquals(uMemberIdL, event.getReceiverId());
        assertEquals(NotificationType.TEAM.toString(), event.getNotificationType());
        assertEquals(teamIdL, event.getContentId());
        assertEquals(1L, event.getSenderId());
        assertEquals(NotificationType.ADD_MEMBER.toString(), event.getContentType());
    }

    @Test
    void testCreate_AccessDenied() {
        when(idEncoder.decode(teamIdS)).thenReturn(teamIdL);
        when(memberRepository.exists(any(Specification.class))).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> {
            memberService.createTeamMember(teamIdS, teamMemberRequest);
        });
        verify(memberRepository, never()).save(any(TeamMember.class));
        verify(applicationEventPublisher, never()).publishEvent(any(NotificationAddEvent.class));
    }

    @Test
    void testCreate_ResourceNotFound() {
        when(idEncoder.decode(teamIdS)).thenReturn(teamIdL);
        when(memberRepository.exists(any(Specification.class))).thenReturn(true);
        when(contactService.isConnected(userId, 2L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> {
            memberService.createTeamMember(teamIdS, teamMemberRequest);
        });
        verify(memberRepository, never()).save(any(TeamMember.class));
        verify(applicationEventPublisher, never()).publishEvent(any(NotificationAddEvent.class));
    }

    @Test
    void testCreate_Duplicate() {
        when(idEncoder.decode(teamIdS)).thenReturn(teamIdL);
        when(memberRepository.exists(any(Specification.class))).thenReturn(true).thenReturn(true);
        when(contactService.isConnected(userId, 2L)).thenReturn(true);
        assertThrows(DuplicateException.class, () -> {
            memberService.createTeamMember(teamIdS, teamMemberRequest);
        });
        verify(memberRepository, never()).save(any(TeamMember.class));
        verify(applicationEventPublisher, never()).publishEvent(any(NotificationAddEvent.class));
    }

    @Test
    void testDelete_Success() {
        String teamMemberIdS = "teamMemberId";
        Long teamMemberIdL = 1L;
        when(idEncoder.decode(teamMemberIdS)).thenReturn(teamMemberIdL);
        TeamMember teamMember = TeamMember.builder().id(teamMemberIdL).team(Team.builder().id(teamIdL).build())
                .user(User.builder().id(uMemberIdL).build()).build();
        when(memberRepository.exists(any(Specification.class))).thenReturn(true);
        when(memberRepository.findById(teamMemberIdL)).thenReturn(Optional.of(teamMember));
        memberService.deleteTeamMember(teamIdS, teamMemberIdS);
        verify(memberRepository, times(1)).deleteById(teamMemberIdL);
        ArgumentCaptor<NotificationAddEvent> captor = ArgumentCaptor.forClass(NotificationAddEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        NotificationAddEvent event = captor.getValue();
        assertEquals(userId, event.getContentId());
        assertEquals(teamIdL, event.getContentId());
        assertEquals(NotificationType.TEAM.toString(), event.getNotificationType());
        assertEquals(NotificationType.REMOVE_MEMBER.toString(), event.getContentType());
    }

    @Test
    void testDelete_AccessDenied() {
        when(memberRepository.exists(any(Specification.class))).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> {
            memberService.deleteTeamMember(teamIdS, uMemberIdS);
        });
        verify(memberRepository, never()).deleteById(anyLong());
        verify(applicationEventPublisher, never()).publishEvent(any(NotificationAddEvent.class));
    }

    @Test
    void testDelete_Forbidden() {
        String teamMemberIdS = "teamMemberId";
        Long teamMemberIdL = 1L;
        when(idEncoder.decode(teamMemberIdS)).thenReturn(teamMemberIdL);
        TeamMember teamMember = TeamMember.builder().id(teamMemberIdL).team(Team.builder().id(teamIdL).build())
                .user(User.builder().id(userId).build()).build();
        when(memberRepository.exists(any(Specification.class))).thenReturn(true);
        when(memberRepository.findById(teamMemberIdL)).thenReturn(Optional.of(teamMember));
        assertThrows(ForbiddenException.class, () -> {
            memberService.deleteTeamMember(teamMemberIdS, teamMemberIdS);
        });
        verify(memberRepository, never()).deleteById(anyLong());
        verify(applicationEventPublisher, never()).publishEvent(any(NotificationAddEvent.class));
    }

    @Test
    void testDelete_ResourcNotFound() {
        String teamMemberIdS = "teamMemberId";
        Long teamMemberIdL = 1L;
        when(idEncoder.decode(teamMemberIdS)).thenReturn(teamMemberIdL);
        when(memberRepository.exists(any(Specification.class))).thenReturn(true);
        when(memberRepository.findById(teamMemberIdL)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            memberService.deleteTeamMember(teamIdS, teamMemberIdS);
        });
        verify(memberRepository, never()).deleteById(anyLong());
        verify(applicationEventPublisher, never()).publishEvent(any(NotificationAddEvent.class));
    }

    @Test
    void testRetrieveList_Success() {
        TeamMemberProjection memberProjection = new TeamMemberProjection() {

            @Override
            public Long getId() {
                return 1L;
            }

            @Override
            public Long getUserId() {
                return userId;
            }

            @Override
            public String getFirstName() {
                return "first name";
            }

            @Override
            public String getLastName() {
                return "last name";
            }

            @Override
            public String getImage() {
                return "image";
            }

            @Override
            public Timestamp getJoinedAt() {
                return Timestamp.valueOf(LocalDateTime.now());
            }

        };
        when(memberRepository.exists(any(Specification.class))).thenReturn(false);
        Pageable pageable = PageRequest.of(0, 10);
        when(memberRepository.getTeamMembers(teamIdL, pageable)).thenReturn(new PageImpl<>(List.of(memberProjection)));
        Page<TeamMemberResponse> memberResponse = memberService.getTeamMembers(teamIdS, pageable);
        verify(memberRepository).getTeamMembers(teamIdL, pageable);
        assertEquals(1, memberResponse.getContent().size());
    }

    @Test
    void testRetrieveList_AccessDenied() {
        Pageable pageable = PageRequest.of(0, 10);
        when(memberRepository.exists(any(Specification.class))).thenReturn(true);
        assertThrows(AccessDeniedException.class, () -> {
            memberService.getTeamMembers(teamIdS, pageable);
        });
    }

    @Test
    void testLeaveTeam_Success() {
        when(memberRepository.exists(any(Specification.class))).thenReturn(true).thenReturn(false);
        when(assignService.isInAnyTask(userId)).thenReturn(false);
        when(memberRepository.delete(any(Specification.class))).thenReturn(1L);
        memberService.leaveTeam(teamIdS);
        verify(memberRepository).delete(any(Specification.class));

        ArgumentCaptor<NotificationAddEvent> captor = ArgumentCaptor.forClass(NotificationAddEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        NotificationAddEvent event = captor.getValue();
        assertEquals(userId, event.getSenderId());
        assertEquals(teamIdL, event.getContentId());
        assertEquals(NotificationType.LEAVE_MEMBER.toString(), event.getContentType());
        assertEquals(NotificationType.TEAM.toString(), event.getNotificationType());
    }

    @Test
    void testLeaveTeam_ResourceNotFound() {
        when(memberRepository.exists(any(Specification.class))).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> {
            memberService.leaveTeam(teamIdS);
        });
    }

    @Test
    void testLeaveTeam_Duplicate() {
        when(memberRepository.exists(any(Specification.class))).thenReturn(true);
        when(assignService.isInAnyTask(userId)).thenReturn(true);
        assertThrows(AccessDeniedException.class, () -> {
            memberService.leaveTeam(teamIdS);
        });
    }
}
