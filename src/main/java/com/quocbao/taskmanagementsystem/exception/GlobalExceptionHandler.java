package com.quocbao.taskmanagementsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.quocbao.taskmanagementsystem.common.ErrorResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(JwtExpiredException.class)
	public ResponseEntity<?> handleJwtExpired(JwtExpiredException ex) {
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setTitle("Jwt token is expired.");
		errorResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
		errorResponse.setDetail(ex.getMessage());
		return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setTitle("Resouce Not Found.");
		errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
		errorResponse.setDetail(ex.getMessage());
		return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setTitle("The provided input is not valid.");
		errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
		errorResponse.setDetail(
				// The exception will be caught in the entity or in the service.
				ex.getConstraintViolations() == null
						// If an exception is encountered in the service.
						// Will get message from ex.getMessage, handle by
						? ex.getMessage()
						// If an exception is encountered in the entity, handle by
						: ex.getConstraintViolations().stream().map(ConstraintViolation::getMessage).toList()
								.getFirst());
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(DuplicateException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateException(DuplicateException ex) {
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setTitle("The provided input already exists.");
		errorResponse.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
		errorResponse.setDetail(ex.getMessage());
		return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
	}
}
