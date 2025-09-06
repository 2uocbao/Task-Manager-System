package com.quocbao.taskmanagementsystem.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.quocbao.taskmanagementsystem.common.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(JwtExpiredException.class)
	public ResponseEntity<?> handleJwtExpired(JwtExpiredException ex) {
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setTitle("Jwt token is expired.");
		errorResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
		errorResponse.setDetail(ex.getMessage());
		LOGGER.error(ex.getMessage());
		return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setTitle("Resouce Not Found.");
		errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
		errorResponse.setDetail(ex.getMessage());
		LOGGER.error(ex.getMessage());
		return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(DuplicateException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateException(DuplicateException ex) {
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setTitle("The provided input already exists.");
		errorResponse.setStatus(HttpStatus.CONFLICT.value());
		errorResponse.setDetail(ex.getMessage());
		LOGGER.error(ex.getMessage());
		return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<ErrorResponse> forbiddenException(ForbiddenException ex) {
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setTitle("The request do not access");
		errorResponse.setStatus(HttpStatus.FORBIDDEN.value());
		errorResponse.setDetail(ex.getMessage());
		LOGGER.error(ex.getMessage());
		return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
	}
}
