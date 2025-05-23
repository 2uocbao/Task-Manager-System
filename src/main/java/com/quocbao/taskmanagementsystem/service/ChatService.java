package com.quocbao.taskmanagementsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.quocbao.taskmanagementsystem.entity.Chat;
import com.quocbao.taskmanagementsystem.payload.response.ChatResponse;
import com.quocbao.taskmanagementsystem.payload.response.UserResponse;

public interface ChatService {

	public ChatResponse createChat(String userId, String withUser);

	public Page<ChatResponse> getChatsByUserId(String userId, Pageable pageable, String keySearch);

	public Chat updateChat(String userId, long chatId, String name);

	public String deleteChat(String userId, long chatId);

	public String addUserToChat(String userId, String memberId, long chatId);

	public String removeUserInChat(String userId, String memberId, long chatId);

	public Page<UserResponse> getUsersInChat(long chatId, Pageable pageable, String keySearch);

	public Page<UserResponse> getUsersConnect(String userId, Pageable pageable, String keySearch);
}
