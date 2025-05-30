package com.quocbao.taskmanagementsystem.security.jwt;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.service.UserService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtTokenProvider jwtTokenProvider;
	private final UserService userService;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserService userService) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.userService = userService;
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {
		try {

			String authHeader = request.getHeader("Authorization");
			String username = null;
			String jwt = null;
			if (authHeader != null && authHeader.startsWith("Bearer ")) {
				jwt = authHeader.substring(7);
				if (StringUtils.hasText(jwt) && Boolean.TRUE.equals(jwtTokenProvider.validationToken(jwt))) {
					username = jwtTokenProvider.extractEmail(jwt);
					User user = userService.getUserByEmail(username);
					if (Boolean.TRUE.equals(jwtTokenProvider.isTokenValid(jwt, user))) {
						UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
								user, null, null);
						usernamePasswordAuthenticationToken
								.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
					}
				}
			}
			filterChain.doFilter(request, response);
		} catch (ExpiredJwtException ex) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
	        response.setContentType("application/json");
	        response.setCharacterEncoding("UTF-8");
	        response.getWriter().write("{\"error\": \"JWT token is expired\"}");
		} catch(JwtException ex) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
	        response.setContentType("application/json");
	        response.setCharacterEncoding("UTF-8");
	        response.getWriter().write("{\"error\": \"JWT token is expired\"}");
		}
	}
}
