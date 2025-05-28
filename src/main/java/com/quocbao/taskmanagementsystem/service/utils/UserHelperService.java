package com.quocbao.taskmanagementsystem.service.utils;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.repository.UserRepository;

@Service
public class UserHelperService {

	private final UserRepository userRepository;
	private final IdEncoder idEncoder;

	public UserHelperService(UserRepository userRepository, IdEncoder idEncoder) {
		this.userRepository = userRepository;
		this.idEncoder = idEncoder;
	}

	public Optional<User> userExist(String userId) {
		return userRepository.findById(idEncoder.decode(userId));
	}

}
