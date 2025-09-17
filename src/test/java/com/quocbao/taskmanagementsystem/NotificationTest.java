package com.quocbao.taskmanagementsystem;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.NotificationType;
import com.quocbao.taskmanagementsystem.entity.Notification;
import com.quocbao.taskmanagementsystem.entity.Task;
import com.quocbao.taskmanagementsystem.entity.Team;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.firebase.FCMService;
import com.quocbao.taskmanagementsystem.repository.NotificationRepository;
import com.quocbao.taskmanagementsystem.service.MessageServiceBundle;
import com.quocbao.taskmanagementsystem.service.utils.TaskHelperService;
import com.quocbao.taskmanagementsystem.service.utils.TeamHelperService;
import com.quocbao.taskmanagementsystem.service.utils.UserHelperService;
import com.quocbao.taskmanagementsystem.serviceimpl.NotificationServiceImpl;

@ExtendWith(MockitoExtension.class)
public class NotificationTest {

    @Mock
    private UserHelperService userHelperService;

    @Mock
    private TeamHelperService teamHelperService;

    @Mock
    private TaskHelperService taskHelperService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private IdEncoder idEncoder;

    @Mock
    private FCMService fcmService;

    @Mock
    private MessageServiceBundle messageServiceBundle = new MessageServiceBundle();

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    public void defaultValue() {
        User user = User.builder().id(1L).firstName("John").lastName("Born").token("10jqk").language("vi").build();
        when(userHelperService.getUser(1L)).thenReturn(Optional.of(user));
        User user2 = User.builder().id(2L).firstName("Jack").lastName("Sparrow").token("910jqk").language("vi").build();
        when(userHelperService.getUser(2L)).thenReturn(Optional.of(user2));
        when(idEncoder.encode(1L)).thenReturn("nguoi");
    }

    @Test
    public void testSaveNotification_Task() {
        Notification notification = Notification.builder().senderId(1L).receiverId(2L).contentId(1L)
                .type(NotificationType.TASK.toString()).typeContent(NotificationType.NEW_ASSIGN.toString()).build();
        Task task = Task.builder().id(1L).title("new task").build();
        when(taskHelperService.getTask(task.getId())).thenReturn(Optional.of(task));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        notificationService.saveNotification(notification);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    public void testSaveNotification_Team() {
        Notification notification = Notification.builder().senderId(1L).receiverId(2L).contentId(1L)
                .type(NotificationType.TEAM.toString()).typeContent(NotificationType.ADD_MEMBER.toString()).build();
        Team team = Team.builder().id(1L).name("New Team").build();
        when(teamHelperService.getTeamById(team.getId())).thenReturn(team);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        notificationService.saveNotification(notification);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    public void testSaveNotification_Contact() {
        Notification notification = Notification.builder().senderId(1L).receiverId(2L).contentId(1L)
                .type(NotificationType.CONTACT.toString()).typeContent(NotificationType.REQUEST.toString()).build();
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        notificationService.saveNotification(notification);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
}
