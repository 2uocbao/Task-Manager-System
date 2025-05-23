package com.quocbao.taskmanagementsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.quocbao.taskmanagementsystem.payload.request.MessageRequest;
import com.quocbao.taskmanagementsystem.payload.response.MessageResponse;

public interface MessageService {

	public void sendMessageToChatId(MessageRequest messageRequest);

	public Page<MessageResponse> getMessagesByChatId(long chatId, Pageable pageable);

	public String deleteMessageByUserId(String messageId, String userId);
}
