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
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.RoleEnum;
import com.quocbao.taskmanagementsystem.entity.Task;
import com.quocbao.taskmanagementsystem.entity.TaskAssignment;
import com.quocbao.taskmanagementsystem.entity.Team;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.events.Notification.NotificationAddEvent;
import com.quocbao.taskmanagementsystem.exception.AccessDeniedException;
import com.quocbao.taskmanagementsystem.exception.DuplicateException;
import com.quocbao.taskmanagementsystem.exception.ForbiddenException;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.TaskAssignRequest;
import com.quocbao.taskmanagementsystem.repository.TaskAssignmentRepository;
import com.quocbao.taskmanagementsystem.service.utils.AuthenticationService;
import com.quocbao.taskmanagementsystem.service.utils.TaskHelperService;
import com.quocbao.taskmanagementsystem.service.utils.TeamMemberHelperService;
import com.quocbao.taskmanagementsystem.service.utils.UserHelperService;
import com.quocbao.taskmanagementsystem.serviceimpl.TaskAssignServiceImpl;

@ExtendWith(MockitoExtension.class)
public class TaskAssigneeTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private TaskAssignmentRepository assignRepository;

    @Mock
    private UserHelperService userHelperService;

    @Mock
    private TeamMemberHelperService teamMemberHelperService;

    @Mock
    private TaskHelperService taskHelperService;

    @Mock
    private AuthenticationService authService;

    @Mock
    private IdEncoder idEncoder;

    @InjectMocks
    private TaskAssignServiceImpl taskAssignService;

    Long userId = 1L;
    Long taskId = 1L;
    String taskIdS = "taskId";

    @BeforeEach
    void defaultValue() {
        when(authService.getUserIdInContext()).thenReturn(userId);
        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
    }

    @Test
    void testAdd_Success() {
        Long teamId = 1L;
        String toUserId = "toUserId";
        Long toUserIdL = 2L;
        TaskAssignRequest taskAssignRequest = new TaskAssignRequest();
        taskAssignRequest.setToUserId(toUserId);
        Task task = Task.builder().id(taskId).team(Team.builder().id(teamId).build()).build();
        when(idEncoder.decode(toUserId)).thenReturn(toUserIdL);
        when(assignRepository.exists(any(Specification.class))).thenReturn(true).thenReturn(false);
        when(taskHelperService.getTask(taskId)).thenReturn(Optional.of(task));
        when(teamMemberHelperService.isMemberTeam(toUserIdL, teamId)).thenReturn(true);
        User user = User.builder().id(toUserIdL).build();
        TaskAssignment taskAssignment = TaskAssignment.builder().id(1L).task(task)
                .user(user).joinedAt(Timestamp.valueOf(LocalDateTime.now()))
                .role(RoleEnum.MEMBER).build();
        when(assignRepository.save(any(TaskAssignment.class))).thenReturn(taskAssignment);
        taskAssignService.addAssign(taskIdS, taskAssignRequest);
        verify(assignRepository, times(2)).exists(any(Specification.class));
        verify(assignRepository).save(any(TaskAssignment.class));
        ArgumentCaptor<NotificationAddEvent> captor = ArgumentCaptor.forClass(NotificationAddEvent.class);
        verify(applicationEventPublisher, times(1)).publishEvent(captor.capture());
        NotificationAddEvent event = captor.getValue();
        assertEquals(event.getSenderId(), userId);
        assertEquals(event.getReceiverId(), toUserIdL);
        assertEquals(event.getContentId(), taskId);
    }

    @Test
    void testAdd_AccessDenied() {
        String toUserId = "toUserId";
        Long toUserIdL = 2L;
        TaskAssignRequest taskAssignRequest = new TaskAssignRequest();
        taskAssignRequest.setToUserId(toUserId);
        when(idEncoder.decode(toUserId)).thenReturn(toUserIdL);
        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        when(assignRepository.exists(any(Specification.class))).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> {
            taskAssignService.addAssign(taskIdS, taskAssignRequest);
        });
        verify(assignRepository, times(1)).exists(any(Specification.class));
        verify(assignRepository, never()).save(any(TaskAssignment.class));
    }

    @Test
    void testAdd_Duplicate() {
        String toUserId = "toUserId";
        Long toUserIdL = 2L;
        TaskAssignRequest taskAssignRequest = new TaskAssignRequest();
        taskAssignRequest.setToUserId(toUserId);
        when(idEncoder.decode(toUserId)).thenReturn(toUserIdL);
        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        when(assignRepository.exists(any(Specification.class))).thenReturn(true).thenReturn(true);
        assertThrows(DuplicateException.class, () -> {
            taskAssignService.addAssign(taskIdS, taskAssignRequest);
        });
        verify(taskHelperService, never()).getTask(anyLong());
        verify(assignRepository, never()).save(any(TaskAssignment.class));
    }

    @Test
    void testAdd_ResourceNotFound() {
        String toUserId = "toUserId";
        Long toUserIdL = 2L;
        TaskAssignRequest taskAssignRequest = new TaskAssignRequest();
        taskAssignRequest.setToUserId(toUserId);
        when(idEncoder.decode(toUserId)).thenReturn(toUserIdL);
        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        when(assignRepository.exists(any(Specification.class))).thenReturn(true).thenReturn(false);
        when(taskHelperService.getTask(taskId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            taskAssignService.addAssign(taskIdS, taskAssignRequest);
        });
        verify(teamMemberHelperService, never()).isMemberTeam(toUserIdL, 1L);
        verify(assignRepository, never()).save(any(TaskAssignment.class));
    }

    @Test
    void testAdd_Forbidden() {
        Long teamId = 1L;
        String toUserId = "toUserId";
        Long toUserIdL = 2L;
        TaskAssignRequest taskAssignRequest = new TaskAssignRequest();
        taskAssignRequest.setToUserId(toUserId);
        Task task = Task.builder().id(taskId).team(Team.builder().id(teamId).build()).build();
        when(idEncoder.decode(toUserId)).thenReturn(toUserIdL);
        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        when(assignRepository.exists(any(Specification.class))).thenReturn(true).thenReturn(false);
        when(taskHelperService.getTask(taskId)).thenReturn(Optional.of(task));
        when(teamMemberHelperService.isMemberTeam(toUserIdL, teamId)).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> {
            taskAssignService.addAssign(taskIdS, taskAssignRequest);
        });
        verify(assignRepository, never()).save(any(TaskAssignment.class));
    }

    @Test
    void testRemove_Success() {
        String assignId = "assignId";
        Long assignIdL = 2L;
        Task task = Task.builder().id(taskId).build();
        User user = User.builder().id(2L).build();
        TaskAssignment taskAssignment = TaskAssignment.builder().id(assignIdL).task(task)
                .user(user).joinedAt(Timestamp.valueOf(LocalDateTime.now()))
                .role(RoleEnum.MEMBER).build();
        TaskAssignRequest taskAssignRequest = new TaskAssignRequest();
        taskAssignRequest.setToUserId("as");
        when(idEncoder.decode(assignId)).thenReturn(assignIdL);
        when(assignRepository.exists(any(Specification.class))).thenReturn(true);
        when(assignRepository.findById(assignIdL)).thenReturn(Optional.of(taskAssignment));
        taskAssignService.removeAssign(taskIdS, assignId, taskAssignRequest);
        verify(assignRepository, times(1)).exists(any(Specification.class));
        verify(assignRepository, times(1)).findById(assignIdL);
        verify(assignRepository, times(1)).delete(taskAssignment);
        ArgumentCaptor<NotificationAddEvent> captor = ArgumentCaptor.forClass(NotificationAddEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        NotificationAddEvent event = captor.getValue();
        assertEquals(userId, event.getSenderId());
        assertEquals(2L, event.getReceiverId());
        assertEquals(taskId, event.getContentId());
    }

    @Test
    void testRemove_AccessDenied() {
        String assignId = "assignId";
        Long assignIdL = 2L;
        TaskAssignRequest taskAssignRequest = new TaskAssignRequest();
        taskAssignRequest.setToUserId("as");
        when(idEncoder.decode(assignId)).thenReturn(assignIdL);
        when(assignRepository.exists(any(Specification.class))).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> {
            taskAssignService.removeAssign(taskIdS, assignId, taskAssignRequest);
        });
        verify(assignRepository, never()).findById(anyLong());
        verify(assignRepository, never()).delete(any(TaskAssignment.class));
    }

    @Test
    void testRemove_Forbidden() {
        String assignId = "assignId";
        Long assignIdL = 2L;
        Task task = Task.builder().id(taskId).build();
        User user = User.builder().id(userId).build();
        TaskAssignRequest taskAssignRequest = new TaskAssignRequest();
        taskAssignRequest.setToUserId("as");
        TaskAssignment taskAssignment = TaskAssignment.builder().id(assignIdL).task(task)
                .user(user).joinedAt(Timestamp.valueOf(LocalDateTime.now()))
                .role(RoleEnum.MEMBER).build();
        when(idEncoder.decode(assignId)).thenReturn(assignIdL);
        when(assignRepository.exists(any(Specification.class))).thenReturn(true);
        when(assignRepository.findById(assignIdL)).thenReturn(Optional.of(taskAssignment));
        assertThrows(ForbiddenException.class, () -> {
            taskAssignService.removeAssign(taskIdS, assignId, taskAssignRequest);
        });
        verify(assignRepository, times(1)).findById(anyLong());
        verify(assignRepository, never()).delete(any(TaskAssignment.class));
    }

    @Test
    void testRetrieveList_AccessDenied() {
        when(assignRepository.exists(any(Specification.class))).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> {
            taskAssignService.getTaskAssigns(taskIdS);
        });
        verify(assignRepository, never()).getTaskAssignments(taskId);
    }
}
