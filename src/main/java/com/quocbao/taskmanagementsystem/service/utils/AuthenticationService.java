package com.quocbao.taskmanagementsystem.service.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.entity.User;

@Service
public class AuthenticationService {

	public Long getUserIdInContext() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = (User) authentication.getPrincipal();
		return user.getId();
	}
}
