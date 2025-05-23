package com.quocbao.taskmanagementsystem.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quocbao.taskmanagementsystem.common.DataResponse;
import com.quocbao.taskmanagementsystem.common.PaginationResponse;
import com.quocbao.taskmanagementsystem.payload.response.ChatResponse;
import com.quocbao.taskmanagementsystem.payload.response.UserResponse;
import com.quocbao.taskmanagementsystem.service.ChatService;

@RestController
@RequestMapping()
public class ChatController {

	private ChatService chatService;

	public ChatController(ChatService chatService) {
		this.chatService = chatService;

	}

	@PostMapping("/chats")
	public ResponseEntity<DataResponse> createChat(@RequestParam String userId, @RequestParam String withUser) {
		return new ResponseEntity<>(new DataResponse(HttpStatus.OK.value(), chatService.createChat(userId, withUser),
				"Chat creation successful."), HttpStatus.OK);
	}

	@GetMapping("/users/{userId}/chats")
	public ResponseEntity<PaginationResponse<ChatResponse>> getChatsByUserId(@PathVariable String userId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String search) {

		Page<ChatResponse> pageChat = chatService.getChatsByUserId(userId, PageRequest.of(0, 10), null);

		List<ChatResponse> entityModel = pageChat.stream().toList();

		PaginationResponse<ChatResponse> paginationResponse = new PaginationResponse<>(HttpStatus.OK, entityModel,
				pageChat.getPageable().getPageNumber(), pageChat.getSize(), pageChat.getTotalElements(),
				pageChat.getTotalPages(), pageChat.getSort().isSorted(), pageChat.getSort().isUnsorted(),
				pageChat.getSort().isEmpty());

		return new ResponseEntity<>(paginationResponse, HttpStatus.OK);
	}

	@DeleteMapping("/chats/{chatId}")
	public ResponseEntity<DataResponse> deleteChat(@RequestParam String userId, @PathVariable long chatId) {
		return new ResponseEntity<>(
				new DataResponse(HttpStatus.OK.value(), null, chatService.deleteChat(userId, chatId)), HttpStatus.OK);
	}

	@PutMapping("/chats/{chatId}")
	public ResponseEntity<DataResponse> updateChat(@RequestParam String userId, @PathVariable long chatId,
			@RequestParam String name) {
		return new ResponseEntity<>(
				new DataResponse(HttpStatus.OK.value(), chatService.updateChat(userId, chatId, name), "Successful"),
				HttpStatus.OK);
	}

	@GetMapping("/chats/{chatId}")
	public ResponseEntity<PaginationResponse<UserResponse>> getUsersInChat(@RequestParam String userId,
			@PathVariable long chatId, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String search) {

		Page<UserResponse> userResponse = chatService.getUsersInChat(chatId, PageRequest.of(page, size), search);

		List<UserResponse> entityModels = userResponse.getContent().stream().toList();

		PaginationResponse<UserResponse> paginationResponse = new PaginationResponse<>(HttpStatus.OK, entityModels,
				userResponse.getNumber(), userResponse.getTotalPages(), userResponse.getTotalElements(),
				userResponse.getSize(), userResponse.getSort().isSorted(), userResponse.getSort().isUnsorted(),
				userResponse.getSort().isEmpty());

		return new ResponseEntity<>(paginationResponse, HttpStatus.OK);
	}

	@PostMapping("/chats/{chatId}/add")
	public ResponseEntity<DataResponse> addMember(@RequestParam String userId, @PathVariable long chatId,
			@RequestParam String memberId) {
		return new ResponseEntity<>(
				new DataResponse(HttpStatus.OK.value(), null, chatService.addUserToChat(userId, memberId, chatId)),
				HttpStatus.OK);
	}

	@DeleteMapping("/chats/{chatId}/delete")
	public ResponseEntity<DataResponse> delMember(@RequestParam String userId, @PathVariable long chatId,
			@RequestParam String memberId) {
		return new ResponseEntity<>(
				new DataResponse(HttpStatus.OK.value(), null, chatService.removeUserInChat(userId, memberId, chatId)),
				HttpStatus.OK);
	}

	@GetMapping("/friends")
	public ResponseEntity<PaginationResponse<UserResponse>> getFriendsOfUser(@RequestParam String userId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "null") String keySearch) {

		Page<UserResponse> usersPage = chatService.getUsersConnect(userId, PageRequest.of(page, size), keySearch);

		List<UserResponse> entityModels = usersPage.getContent().stream().toList();

		PaginationResponse<UserResponse> paginationResponse = new PaginationResponse<>(HttpStatus.OK, entityModels,
				usersPage.getNumber(), usersPage.getTotalPages(), usersPage.getTotalElements(), usersPage.getSize(),
				usersPage.getSort().isSorted(), usersPage.getSort().isUnsorted(), usersPage.getSort().isEmpty());

		return new ResponseEntity<>(paginationResponse, HttpStatus.OK);
	}
}
