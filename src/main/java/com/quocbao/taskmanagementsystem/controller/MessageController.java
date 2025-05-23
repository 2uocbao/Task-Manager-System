package com.quocbao.taskmanagementsystem.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.quocbao.taskmanagementsystem.common.PaginationResponse;
import com.quocbao.taskmanagementsystem.payload.request.MessageRequest;
import com.quocbao.taskmanagementsystem.payload.response.MessageResponse;
import com.quocbao.taskmanagementsystem.service.MessageService;

@Controller
public class MessageController {

	private final MessageService messageService;

	public MessageController(MessageService messageService) {
		this.messageService = messageService;
	}

	@MessageMapping("/chat")
	public MessageRequest sendSpecific(@Payload MessageRequest message) {
		messageService.sendMessageToChatId(message);
		return message;
	}

	@GetMapping("/message/{groupId}")
	public ResponseEntity<PaginationResponse<MessageResponse>> getMessagesByGroupId(@PathVariable int groupId,
			@RequestParam(defaultValue = "page") int page, @RequestParam(defaultValue = "size") int size) {

		Direction direction = Direction.DESC;
		Page<MessageResponse> messageResponse = messageService.getMessagesByChatId(groupId,
				PageRequest.of(page, size, direction, "createdAt"));

		List<MessageResponse> entityModels = messageResponse.getContent().stream().toList();

		PaginationResponse<MessageResponse> paginationResponse = new PaginationResponse<>(HttpStatus.OK, entityModels,
				messageResponse.getPageable().getPageNumber(), messageResponse.getSize(),
				messageResponse.getTotalElements(), messageResponse.getTotalPages(),
				messageResponse.getSort().isSorted(), messageResponse.getSort().isUnsorted(),
				messageResponse.getSort().isEmpty());

		return new ResponseEntity<>(paginationResponse, HttpStatus.OK);
	}
}
