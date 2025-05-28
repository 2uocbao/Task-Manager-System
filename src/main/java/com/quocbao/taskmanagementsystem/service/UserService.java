package com.quocbao.taskmanagementsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.payload.request.FcmRequest;
import com.quocbao.taskmanagementsystem.payload.response.UserResponse;

public interface UserService {
	public User getUserByEmail(String email);

	public Page<UserResponse> searchUser(String userId, String keySearch, Pageable pageable);

	public void addToken(FcmRequest fcmRequest);

	public UserResponse getUser(String userId);

}
