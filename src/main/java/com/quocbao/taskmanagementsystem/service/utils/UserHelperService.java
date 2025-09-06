package com.quocbao.taskmanagementsystem.service.utils;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.repository.UserRepository;
import com.quocbao.taskmanagementsystem.specifications.UserSpecification;

@Service
public class UserHelperService {

	private final UserRepository userRepository;

	public UserHelperService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public Boolean isUserExist(Long userId) {
		return userRepository.exists(UserSpecification.getUserById(userId));
	}

	public Optional<User> getUser(Long userId) {
		return userRepository.findById(userId);
	}

	public String getToken(Long userId) {
		return userRepository.getTokenOfUser(userId).getToken();
	}

}
