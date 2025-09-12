package com.quocbao.taskmanagementsystem.serviceimpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.FcmRequest;
import com.quocbao.taskmanagementsystem.payload.response.UserResponse;
import com.quocbao.taskmanagementsystem.repository.UserRepository;
import com.quocbao.taskmanagementsystem.service.UserService;
import com.quocbao.taskmanagementsystem.service.utils.AuthenticationService;

@Service
public class UserServiceImpl implements UserService {

	static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

	private final UserRepository userRepository;

	private final AuthenticationService authService;

	private final IdEncoder idEncoder;

	public UserServiceImpl(UserRepository userRepository, AuthenticationService authService, IdEncoder idEncoder) {
		this.userRepository = userRepository;
		this.authService = authService;
		this.idEncoder = idEncoder;
	}

	@Override
	public User getUserByEmail(String email) {
		User user = userRepository.findByEmail(email);
		if (user == null) {
			throw new ResourceNotFoundException("User can not found");
		}
		return user;
	}

	@Override
	public Page<UserResponse> searchUser(String keyword, Pageable pageable) {
		return userRepository.searchUser(authService.getUserIdInContext(), keyword, pageable)
				.map(t -> new UserResponse(idEncoder.encode(t.getId()), t.getFirst(), t.getLast(), t.getImage(),
						t.getEmail()));
	}

	@Override
	public void addToken(FcmRequest fcmRequest) {
		userRepository.findById(authService.getUserIdInContext()).ifPresent(t -> {
			t.setToken(fcmRequest.getFcmToken());
			t.setLanguage(fcmRequest.getLanguage());
			userRepository.save(t);
		});
		LOGGER.info("Update fcm token and language to user");
	}

	@Override
	@Cacheable(value = "userId", key = "#userId")
	public User getUser(String userId) {
		return userRepository.findById(idEncoder.decode(userId)).get();
	}

	@Override
	public void removeToken() {
		Long currentUserId = authService.getUserIdInContext();
		userRepository.findById(currentUserId).ifPresent(t -> {
			t.setToken(null);
			t.setLanguage(null);
			userRepository.save(t);
		});
		LOGGER.info("Remove token of " + currentUserId);
	}
}
