package com.quocbao.taskmanagementsystem;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.entity.Comment;
import com.quocbao.taskmanagementsystem.entity.Mention;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.events.Notification.NotificationAddEvent;
import com.quocbao.taskmanagementsystem.repository.MentionRepository;
import com.quocbao.taskmanagementsystem.service.utils.CommentHelperService;
import com.quocbao.taskmanagementsystem.service.utils.UserHelperService;
import com.quocbao.taskmanagementsystem.serviceimpl.MentionServiceImpl;

@ExtendWith(MockitoExtension.class)
public class MentionTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private CommentHelperService commentHelperService;

    @Mock
    private UserHelperService userHelperService;

    @Mock
    private MentionRepository mentionRepository;

    @Mock
    private IdEncoder idEncoder;

    @InjectMocks
    private MentionServiceImpl mentionServiceImpl;

    Long userId = 1L;
    Long commentId = 2L;

    @Test
    void testCreate() {
        List<String> mentionIdLs = new ArrayList<>();
        mentionIdLs.add("1");
        mentionIdLs.add("2");
        mentionIdLs.add("3");
        Long userId1 = 2L;
        Long userId2 = 3L;
        Long userId3 = 4L;
        when(idEncoder.decode("1")).thenReturn(userId1);
        when(idEncoder.decode("2")).thenReturn(userId2);
        when(idEncoder.decode("3")).thenReturn(userId3);
        User user1 = User.builder().id(userId1).build();
        User user2 = User.builder().id(userId2).build();
        User user3 = User.builder().id(userId3).build();
        Comment comment = Comment.builder().id(commentId).build();
        when(commentHelperService.getCommentById(commentId)).thenReturn(comment);
        when(userHelperService.getUser(userId1)).thenReturn(Optional.of(user1));
        when(userHelperService.getUser(userId2)).thenReturn(Optional.of(user2));
        when(userHelperService.getUser(userId3)).thenReturn(Optional.of(user3));
        mentionServiceImpl.createMention(userId, commentId, mentionIdLs);
        verify(mentionRepository, times(3)).save(any(Mention.class));
        verify(applicationEventPublisher, times(3)).publishEvent(any(NotificationAddEvent.class));
    }

    @Test
    void testUpdate() {
        Mention mention = Mention.builder().id(1L).user(User.builder().id(2L).build()).build();
        Mention mention1 = Mention.builder().id(2L).user(User.builder().id(3L).build()).build();

        mentionRepository.saveAll(List.of(mention, mention1));

        List<String> mentionId = new ArrayList<>();

        mentionId.add("1");
        mentionId.add("2");

        when(idEncoder.decode("1")).thenReturn(2L);
        when(idEncoder.decode("2")).thenReturn(3L);

        List<Mention> mentions = new ArrayList<>(List.of(mention, mention1));

        when(mentionRepository.findAll(any(Specification.class))).thenReturn(mentions);

        mentionServiceImpl.updateMention(userId, commentId, mentionId);
    }
}
