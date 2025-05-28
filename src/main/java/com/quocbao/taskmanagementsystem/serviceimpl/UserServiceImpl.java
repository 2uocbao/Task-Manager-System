package com.quocbao.taskmanagementsystem.serviceimpl;

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

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final IdEncoder idEncoder;

	public UserServiceImpl(UserRepository userRepository, IdEncoder idEncoder) {
		this.userRepository = userRepository;
		this.idEncoder = idEncoder;
	}

	@Override
	public Page<UserResponse> searchUser(String userId, String keySearch, Pageable pageable) {
		return userRepository.searchUser(idEncoder.decode(userId), keySearch, pageable)
				.map(t -> new UserResponse(idEncoder.endcode(t.getId()), t.getFirst(), t.getLast(), t.getImage(),
						t.getEmail()));
	}

	@Override
	public User getUserByEmail(String email) {
		User user = userRepository.findByEmail(email);
		if (user == null) {
			throw new ResourceNotFoundException("User not found");
		}
		return user;
	}

	@Override
	public void addToken(FcmRequest fcmRequest) {
		userRepository.findById(idEncoder.decode(fcmRequest.getUserId())).ifPresentOrElse(t -> {
			t.setToken(fcmRequest.getFcmToken());
			t.setLanguage(fcmRequest.getLanguage());
			userRepository.save(t);
		}, () -> new ResourceNotFoundException());
	}

	@Override
	public UserResponse getUser(String userId) {
		User user = userRepository.findById(idEncoder.decode(userId)).get();
		return new UserResponse(userId, user.getFirstName(), user.getLastName(), user.getImage(), null);
	}

}
