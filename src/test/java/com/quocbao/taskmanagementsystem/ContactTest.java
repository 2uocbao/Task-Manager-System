package com.quocbao.taskmanagementsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.quocbao.taskmanagementsystem.common.MethodGeneral;
import com.quocbao.taskmanagementsystem.common.NotificationType;
import com.quocbao.taskmanagementsystem.common.StatusEnum;
import com.quocbao.taskmanagementsystem.entity.Contacts;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.events.Notification.NotificationAddEvent;
import com.quocbao.taskmanagementsystem.exception.AccessDeniedException;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.ContactRequest;
import com.quocbao.taskmanagementsystem.repository.ContactRepository;
import com.quocbao.taskmanagementsystem.service.utils.AuthenticationService;
import com.quocbao.taskmanagementsystem.service.utils.UserHelperService;
import com.quocbao.taskmanagementsystem.serviceimpl.ContactServiceImpl;

@ExtendWith(MockitoExtension.class)
public class ContactTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private UserHelperService userHelperService;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private IdEncoder idEncoder;

    @Mock
    private MethodGeneral methodGeneral;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ContactServiceImpl contactServiceImpl;

    Long userId = 1L;

    @BeforeEach
    void defaultValue() {
        when(authenticationService.getUserIdInContext()).thenReturn(userId);
    }

    @Test
    void testCreate_Success() {
        String toUserId = "touserId";
        Long toUserIdL = 2L;
        ContactRequest contactRequest = new ContactRequest();
        contactRequest.setToUser(toUserId);
        when(idEncoder.decode(toUserId)).thenReturn(toUserIdL);
        when(contactRepository.exists(any(Specification.class))).thenReturn(false);
        when(userHelperService.isUserExist(toUserIdL)).thenReturn(true);
        User fromUser = User.builder().id(userId).build();
        User toUser = User.builder().id(toUserIdL).build();
        Contacts contacts = Contacts.builder().user(fromUser).friendId(toUser).statusEnum(StatusEnum.REQUESTED)
                .build();
        when(contactRepository.save(any(Contacts.class))).thenReturn(contacts);
        contactServiceImpl.createContact(contactRequest);
        verify(contactRepository, times(1)).exists(any(Specification.class));
        verify(contactRepository, times(1)).save(any(Contacts.class));
        ArgumentCaptor<NotificationAddEvent> captor = ArgumentCaptor.forClass(NotificationAddEvent.class);
        verify(applicationEventPublisher, times(1)).publishEvent(captor.capture());
        NotificationAddEvent event = captor.getValue();
        assertEquals(userId, event.getSenderId());
        assertEquals(toUserIdL, event.getReceiverId());
        assertEquals(NotificationType.CONTACT.toString(), event.getNotificationType());
        assertEquals(NotificationType.REQUEST.toString(), event.getContentType());
    }

    @Test
    void testCreate_AccessDenied() {
        String toUserId = "touserId";
        Long toUserIdL = 1L;
        ContactRequest contactRequest = new ContactRequest();
        contactRequest.setToUser(toUserId);
        when(idEncoder.decode(toUserId)).thenReturn(toUserIdL);
        // when both user was connected
        when(contactRepository.exists(any(Specification.class))).thenReturn(false);
        // when user id request is the same user id receiver
        // set to userId == userId
        assertThrows(AccessDeniedException.class, () -> {
            contactServiceImpl.createContact(contactRequest);
        });
        verify(userHelperService, never()).isUserExist(toUserIdL);
        verify(applicationEventPublisher, never()).publishEvent(NotificationAddEvent.class);
    }

    @Test
    void testCreate_ResourceNotFound() {
        String toUserId = "touserId";
        Long toUserIdL = 2L;
        ContactRequest contactRequest = new ContactRequest();
        contactRequest.setToUser(toUserId);
        when(idEncoder.decode(toUserId)).thenReturn(toUserIdL);
        when(contactRepository.exists(any(Specification.class))).thenReturn(false);
        when(userHelperService.isUserExist(toUserIdL)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> {
            contactServiceImpl.createContact(contactRequest);
        });
        verify(contactRepository, never()).save(any(Contacts.class));
    }

    @Test
    void testUpdate_Success() {
        String contactId = "contactId";
        Long contactIdLong = 2L;
        when(idEncoder.decode(contactId)).thenReturn(contactIdLong);
        Contacts contact = Contacts.builder().id(contactIdLong).user(User.builder().id(2L).build())
                .friendId(User.builder().id(userId).build()).build();
        when(contactRepository.findById(contactIdLong)).thenReturn(Optional.of(contact));
        contactServiceImpl.updateContact(contactId);
        verify(contactRepository, times(1)).findById(contactIdLong);
        verify(contactRepository, times(1)).save(any(Contacts.class));
        ArgumentCaptor<NotificationAddEvent> captor = ArgumentCaptor.forClass(NotificationAddEvent.class);
        verify(applicationEventPublisher, times(1)).publishEvent(captor.capture());
        NotificationAddEvent event = captor.getValue();
        assertEquals(userId, contact.getFriendId().getId());
        assertNotEquals(userId, contact.getUser().getId());
        assertEquals(userId, event.getSenderId());
        assertEquals(2L, event.getReceiverId());
        assertEquals(NotificationType.CONTACTACEPT.toString(), event.getContentType());
    }

    @Test
    void testUpdate_AccessDenied() {
        String contactId = "contactId";
        Long contactIdLong = 2L;
        when(idEncoder.decode(contactId)).thenReturn(contactIdLong);
        Contacts contact = Contacts.builder().id(contactIdLong).user(User.builder().id(3L).build())
                .friendId(User.builder().id(2L).build()).build();
        when(contactRepository.findById(contactIdLong)).thenReturn(Optional.of(contact));
        assertThrows(AccessDeniedException.class, () -> {
            contactServiceImpl.updateContact(contactId);
        });
        verify(contactRepository, never()).save(any(Contacts.class));
    }

    @Test
    void testUpdate_ResourceNotFound() {
        String contactId = "contactId";
        Long contactIdLong = 2L;
        when(idEncoder.decode(contactId)).thenReturn(contactIdLong);
        when(contactRepository.findById(contactIdLong)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            contactServiceImpl.updateContact(contactId);
        });
        verify(contactRepository, never()).save(any(Contacts.class));
    }

    @Test
    void testDelete_Success() {
        String contactIdS = "contactId";
        Long contactId = 1L;
        Contacts contact = Contacts.builder().id(contactId).user(User.builder().id(2L).build())
                .friendId(User.builder().id(userId).build()).build();
        when(idEncoder.decode(contactIdS)).thenReturn(contactId);
        when(contactRepository.findById(contactId)).thenReturn(Optional.of(contact));
        contactServiceImpl.deleteContact(contactIdS);
        verify(contactRepository, times(1)).findById(contactId);
        verify(contactRepository, times(1)).delete(contact);
    }

    @Test
    void testDelete_ResourceNotFound() {
        String contactIdS = "contactId";
        Long contactId = 1L;
        when(idEncoder.decode(contactIdS)).thenReturn(contactId);
        when(contactRepository.findById(contactId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            contactServiceImpl.deleteContact(contactIdS);
        });
        verify(contactRepository, never()).delete(any(Contacts.class));
    }
}
