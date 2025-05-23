package com.quocbao.taskmanagementsystem.common;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.quocbao.taskmanagementsystem.exception.UnauthorizedException;

@Component
public class MethodGeneral {

	private MethodGeneral() {

	}

	public void validatePermission(long userId, long currentUserId) {
		if (!Objects.equals(userId, currentUserId)) {
			throw new UnauthorizedException("User does not have permission.");
		}
	}

	public void havePermission(Long userId, Long userId1, Long userId2) {
		if (userId == userId1 || userId == userId2) {
			return;
		}
		throw new UnauthorizedException("User does not have permission.");
	}
}
