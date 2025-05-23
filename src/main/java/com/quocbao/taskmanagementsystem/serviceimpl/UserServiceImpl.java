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
	private final IdEncoder idEndcoder;

	public UserServiceImpl(UserRepository userRepository, IdEncoder idEndcoder) {
		this.userRepository = userRepository;
		this.idEndcoder = idEndcoder;
	}

	@Override
	public Page<UserResponse> searchUser(String userId, String keySearch, Pageable pageable) {
		return userRepository.searchUser(idEndcoder.decode(userId), keySearch, pageable)
				.map(t -> new UserResponse(idEndcoder.endcode(t.getId()), t.getFirst(), t.getLast(), t.getImage(),
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
	public String getToken(Long userId) {
		User user = userRepository.findById(userId).get();
		return user.getToken();
	}

	@Override
	public void addToken(FcmRequest fcmRequest) {
		userRepository.findById(idEndcoder.decode(fcmRequest.getUserId())).ifPresentOrElse(t -> {
			t.setToken(fcmRequest.getFcmToken());
			userRepository.save(t);
		}, () -> new ResourceNotFoundException());
	}
}
