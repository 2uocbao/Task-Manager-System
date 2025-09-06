package com.quocbao.taskmanagementsystem;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.repository.UserRepository;
import com.quocbao.taskmanagementsystem.serviceimpl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserTest {

	@Mock
	private UserRepository userRepository;
	
	@InjectMocks
	private UserServiceImpl userService;
	
	@Test
	void testGetUserByEmail_Success() {
		String email = "abc@gmail.com";
		User user = User.builder().email(email).build();
		
		when(userRepository.findByEmail(email)).thenReturn(user);
		
		User result = userService.getUserByEmail(email);
		
		assertNotNull(result);
		assertEquals(email, result.getEmail());
		verify(userRepository).findByEmail(email);
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
}
