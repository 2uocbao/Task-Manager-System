package com.quocbao.taskmanagementsystem;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.FcmRequest;
import com.quocbao.taskmanagementsystem.repository.UserRepository;
import com.quocbao.taskmanagementsystem.service.utils.AuthenticationService;
import com.quocbao.taskmanagementsystem.serviceimpl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private AuthenticationService authenticationService;

	@InjectMocks
	private UserServiceImpl userService;

	@BeforeEach
	void defaultData() {
		Long userId = 1L;
		when(authenticationService.getUserIdInContext()).thenReturn(userId);
	}

	@Test
	void testGetUserByEmail_Success() {
		String email = "abc@gmail.com";
		User user = User.builder().email(email).build();

		when(userRepository.findByEmail(email)).thenReturn(user);

		User result = userService.getUserByEmail(email);

		assertEquals(email, result.getEmail());
	}

	@Test
	void testGetUserByEmail_NotFound() {
		// Arrange
		String email = "notfound@example.com";
		when(userRepository.findByEmail(email)).thenReturn(null);
		// Act & Assert
		assertThrows(ResourceNotFoundException.class, () -> {
			userService.getUserByEmail(email);
		});
		verify(userRepository).findByEmail(email);
	}

	@Test
	void testUpdateFCMToken_Success() {
		User user = User.builder().id(1L).build();
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

		FcmRequest fcmRequest = new FcmRequest();
		fcmRequest.setLanguage("vn");
		fcmRequest.setFcmToken("network");

		userService.addToken(fcmRequest);
		System.out.println(user.getToken());
		System.out.println(user.getLanguage());
		verify(userRepository).findById(user.getId());
		verify(userRepository).save(user);
	}

	@Test
	void testRemoveFCMToken_Success() {
		User user = User.builder().id(1L).token("fcmtoken").language("vn").build();
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		userService.removeToken();
		// System.out.println(user.getToken());
		// System.out.println(user.getLanguage());
		verify(userRepository).findById(user.getId());
		verify(userRepository).save(user);
	}
}
