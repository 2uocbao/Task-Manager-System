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
import java.util.ArrayList;
import java.util.Collections;
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
import org.springframework.data.domain.PageRequest;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.RoleEnum;
import com.quocbao.taskmanagementsystem.entity.Comment;
import com.quocbao.taskmanagementsystem.entity.Task;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.events.Mention.MentionAddEvent;
import com.quocbao.taskmanagementsystem.events.Mention.MentionUpdateEvent;
import com.quocbao.taskmanagementsystem.exception.AccessDeniedException;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.CommentRequest;
import com.quocbao.taskmanagementsystem.payload.response.CommentResponse;
import com.quocbao.taskmanagementsystem.repository.CommentRepository;
import com.quocbao.taskmanagementsystem.service.utils.AuthenticationService;
import com.quocbao.taskmanagementsystem.service.utils.TaskAssignmentHelperService;
import com.quocbao.taskmanagementsystem.service.utils.TaskHelperService;
import com.quocbao.taskmanagementsystem.serviceimpl.CommentServiceImpl;

@ExtendWith(MockitoExtension.class)
public class CommentTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private AuthenticationService authService;

    @Mock
    private TaskAssignmentHelperService taskAssignHelperService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskHelperService taskHelperService;

    @Mock
    private IdEncoder idEncoder;

    @InjectMocks
    private CommentServiceImpl commentService;

    Long userId = 1L;
    String taskId = "taskId";
    Long taskIdL = 1L;

    @BeforeEach
    void defaultData() {
        when(authService.getUserIdInContext()).thenReturn(userId);
        // when(idEncoder.decode(taskId)).thenReturn(taskIdL);
    }

    @Test
    void testCreate_SuccessWithThoutMention() {
        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setText("Hello");
        commentRequest.setMention(Collections.emptyList());

        when(taskHelperService.isTaskExist(taskIdL)).thenReturn(true);
        when(taskAssignHelperService.isUserInTask(userId, taskIdL)).thenReturn(true);

        Comment comment = Comment.builder().id(1L).text("Hello").createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponse commentResponse = commentService.createComment(taskId,
                commentRequest);

        assertEquals(commentRequest.getText(), commentResponse.getText());

        verify(commentRepository, times(1)).save(any(Comment.class));
        verify(applicationEventPublisher, never()).publishEvent(MentionAddEvent.class);
    }

    @Test
    void testCreate_SuccessWithMention() {
        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setText("Hello");
        commentRequest.setMention(new ArrayList<>(List.of("1", "2")));

        when(taskHelperService.isTaskExist(taskIdL)).thenReturn(true);
        when(taskAssignHelperService.isUserInTask(userId, taskIdL)).thenReturn(true);

        Comment comment = Comment.builder().id(1L).text("Hello").createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponse commentResponse = commentService.createComment(taskId,
                commentRequest);

        assertEquals(commentRequest.getText(), commentResponse.getText());

        verify(commentRepository, times(1)).save(any(Comment.class));
        ArgumentCaptor<MentionAddEvent> captor = ArgumentCaptor.forClass(MentionAddEvent.class);
        verify(applicationEventPublisher, times(1)).publishEvent(captor.capture());
        MentionAddEvent event = captor.getValue();
        assertEquals(userId, event.getUserId());
        assertEquals(comment.getId(), event.getCommentId());
        assertEquals(commentRequest.getMention(), event.getMentionId());
    }

    @Test
    void testCreate_ResourceNotFound() {
        CommentRequest commentRequest = new CommentRequest();
        when(taskHelperService.isTaskExist(taskIdL)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.createComment(taskId, commentRequest);
        });
        verify(commentRepository, never()).save(any(Comment.class));
        verify(applicationEventPublisher, never()).publishEvent(any(MentionAddEvent.class));
    }

    @Test
    void testCreate_AccessDenied() {
        CommentRequest commentRequest = new CommentRequest();
        when(taskHelperService.isTaskExist(taskIdL)).thenReturn(true);
        when(taskAssignHelperService.isUserInTask(userId, taskIdL)).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> {
            commentService.createComment(taskId, commentRequest);
        });
        verify(commentRepository, never()).save(any(Comment.class));
        verify(applicationEventPublisher, never()).publishEvent(any(MentionAddEvent.class));
    }

    @Test
    void testUpdate_Success() {
        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setText("Hello");
        commentRequest.setMention(new ArrayList<>(List.of("1", "2")));
        String commentId = "commentId";
        Long commentIdL = 1L;
        Comment comment = Comment.builder().id(commentIdL).text("Hello")
                .user(User.builder().id(userId).build())
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        when(idEncoder.decode(commentId)).thenReturn(commentIdL);
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        commentService.updateComment(commentId, commentRequest);
        verify(commentRepository, times(1)).findById(commentIdL);
        verify(commentRepository, times(1)).save(any(Comment.class));
        verify(applicationEventPublisher, times(1)).publishEvent(any(MentionUpdateEvent.class));
    }

    @Test
    void testUpdate_AccessDenied() {
        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setText("Hello");
        commentRequest.setMention(new ArrayList<>(List.of("1", "2")));
        String commentId = "commentId";
        Long commentIdL = 1L;
        Comment comment = Comment.builder().id(commentIdL).text("Hello")
                .user(User.builder().id(2L).build())
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        when(idEncoder.decode(commentId)).thenReturn(commentIdL);
        when(commentRepository.findById(commentIdL)).thenReturn(Optional.of(comment));
        assertThrows(AccessDeniedException.class, () -> {
            commentService.updateComment(commentId, commentRequest);
        });
        verify(commentRepository, never()).save(any(Comment.class));
        verify(applicationEventPublisher, never()).publishEvent(any(MentionUpdateEvent.class));
    }

    @Test
    void testUpdate_ResourceNotFound() {
        CommentRequest commentRequest = new CommentRequest();
        String commentId = "commentId";
        Long commentIdL = 1L;
        when(idEncoder.decode(commentId)).thenReturn(commentIdL);
        when(commentRepository.findById(commentIdL)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.updateComment(commentId, commentRequest);
        });
        verify(commentRepository, never()).save(any(Comment.class));
        verify(applicationEventPublisher, never()).publishEvent(any(MentionUpdateEvent.class));
    }

    @Test
    void testRetrieveList_AccessDenied() {
        when(taskAssignHelperService.isUserInTask(userId, taskIdL)).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> {
            commentService.getCommentsofTask(taskId, PageRequest.of(0, 10));
        });
        verify(commentRepository, never()).getCommentsByTaskIds(taskIdL, PageRequest.of(0, 10));
    }

    @Test
    void testDelete_SuccessWithCreatorComment() {
        String commentId = "commentId";
        Long commentIdL = 1L;
        when(idEncoder.decode(commentId)).thenReturn(commentIdL);
        Comment comment = Comment.builder().id(commentIdL).text("Hello")
                .user(User.builder().id(userId).build())
                .task(Task.builder().id(taskIdL).build())
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        when(commentRepository.findById(commentIdL)).thenReturn(Optional.of(comment));
        commentService.deleteComment(commentId);
        verify(commentRepository, times(1)).findById(commentIdL);
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void testDelete_SuccessWithCreatorTask() {
        String commentId = "commentId";
        Long commentIdL = 1L;
        when(idEncoder.decode(commentId)).thenReturn(commentIdL);
        Comment comment = Comment.builder().id(commentIdL).text("Hello")
                .user(User.builder().id(2L).build())
                .task(Task.builder().id(taskIdL).build())
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        when(taskAssignHelperService.isRoleUserInTask(userId, comment.getTask().getId(), RoleEnum.ADMIN))
                .thenReturn(true);
        when(commentRepository.findById(commentIdL)).thenReturn(Optional.of(comment));
        commentService.deleteComment(commentId);
        verify(commentRepository, times(1)).findById(commentIdL);
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void testDelete_AccessDeniedWithNoCreatorTask() {
        String commentId = "commentId";
        Long commentIdL = 1L;
        when(idEncoder.decode(commentId)).thenReturn(commentIdL);
        Comment comment = Comment.builder().id(commentIdL).text("Hello")
                .user(User.builder().id(2L).build())
                .task(Task.builder().id(taskIdL).build())
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        when(taskAssignHelperService.isRoleUserInTask(userId, comment.getTask().getId(), RoleEnum.ADMIN))
                .thenReturn(false);
        when(commentRepository.findById(commentIdL)).thenReturn(Optional.of(comment));
        assertThrows(AccessDeniedException.class, () -> {
            commentService.deleteComment(commentId);
        });
        verify(commentRepository, never()).delete(comment);
    }

    @Test
    void testDelete_AccessDeniedWithNoCreatorCommentAndTask() {
        String commentId = "commentId";
        Long commentIdL = 1L;
        when(idEncoder.decode(commentId)).thenReturn(commentIdL);
        Comment comment = Comment.builder().id(commentIdL).text("Hello")
                .user(User.builder().id(2L).build())
                .task(Task.builder().id(taskIdL).build())
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        when(taskAssignHelperService.isRoleUserInTask(userId, comment.getTask().getId(), RoleEnum.ADMIN))
                .thenReturn(false);
        when(commentRepository.findById(commentIdL)).thenReturn(Optional.of(comment));
        assertThrows(AccessDeniedException.class, () -> {
            commentService.deleteComment(commentId);
        });
        verify(commentRepository, never()).delete(comment);
    }
}
