package com.quocbao.taskmanagementsystem.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quocbao.taskmanagementsystem.common.DataResponse;
import com.quocbao.taskmanagementsystem.common.PaginationResponse;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.exception.JwtExpiredException;
import com.quocbao.taskmanagementsystem.payload.request.FcmRequest;
import com.quocbao.taskmanagementsystem.payload.request.RefreshRequest;
import com.quocbao.taskmanagementsystem.payload.response.RefreshResponse;
import com.quocbao.taskmanagementsystem.payload.response.UserResponse;
import com.quocbao.taskmanagementsystem.security.jwt.JwtTokenProvider;
import com.quocbao.taskmanagementsystem.service.UserService;

import io.jsonwebtoken.JwtException;

@RestController
@RequestMapping("/users")
public class UserController {

	private final UserService userService;

	private final JwtTokenProvider jwtTokenProvider;

	public UserController(UserService userService, JwtTokenProvider jwtTokenProvider) {
		this.userService = userService;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@GetMapping()
	public DataResponse getUserByEmail(@RequestHeader("Authorization") String authHeader) {
		String idToken = authHeader.replace("Bearer ", "");
		String userId = jwtTokenProvider.extractUserId(idToken);
		return new DataResponse(HttpStatus.OK.value(), new UserResponse(userService.getUser(userId)), "Successful");
	}

	@GetMapping("/refresh-token")
	public DataResponse refreshToken(@RequestBody RefreshRequest refreshRequest) {
		String refreshToken = refreshRequest.getRefreshToken();
		String newToken = null;
		try {
			jwtTokenProvider.validationToken(refreshToken);
		} catch (JwtException e) {
			throw new JwtExpiredException("JWT token is expired");
		}

		String userId = jwtTokenProvider.extractUserId(refreshToken);
		User user = userService.getUser(userId);
		newToken = jwtTokenProvider.generateToken(user);
		return new DataResponse(HttpStatus.OK.value(), new RefreshResponse(newToken), "Successful");
	}

	@GetMapping("/searchs")
	public PaginationResponse<UserResponse> searchUser(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String keyword) {

		Page<UserResponse> userResponse = userService.searchUser(keyword, PageRequest.of(page, size));

		List<UserResponse> entityModels = userResponse.getContent().stream().toList();

		PaginationResponse<UserResponse> paginationResponse = new PaginationResponse<>(HttpStatus.OK, entityModels,
				userResponse.getPageable().getPageNumber(), userResponse.getSize(), userResponse.getTotalElements(),
				userResponse.getTotalPages(), userResponse.getSort().isSorted(), userResponse.getSort().isUnsorted(),
				userResponse.getSort().isEmpty());

		return paginationResponse;
	}

	@PutMapping("/fcm_token")
	public void addToken(@RequestBody FcmRequest fcmRequest) {
		userService.addToken(fcmRequest);
	}

	@DeleteMapping("/remove_fcm_token")
	public void removeToken() {
		userService.removeToken();
	}

	@GetMapping("/{userId}/mentions")
	public DataResponse getUser(@PathVariable String userId) {
		return new DataResponse(HttpStatus.OK.value(), new UserResponse(userService.getUser(userId)), "success");
	}

}
