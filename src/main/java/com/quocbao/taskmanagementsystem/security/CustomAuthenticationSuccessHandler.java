package com.quocbao.taskmanagementsystem.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.repository.UserRepository;
import com.quocbao.taskmanagementsystem.security.jwt.JwtTokenProvider;
import com.quocbao.taskmanagementsystem.security.oauthconfig.CustomOauth2User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private final UserRepository userRepository;

	private final JwtTokenProvider jwtTokenProvider;

	public CustomAuthenticationSuccessHandler(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {

		this.userRepository = userRepository;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		if (authentication instanceof OAuth2AuthenticationToken _) {
			CustomOauth2User oAuth2User = new CustomOauth2User((OAuth2User) authentication.getPrincipal());

			User user = userRepository.findByEmail(oAuth2User.getEmail());
			if (user.getId() != null) {
				user.setFirstName(oAuth2User.getAttribute("given_name"));
				user.setLastName(oAuth2User.getAttribute("family_name"));
				user.setImage(oAuth2User.getAttribute("picture"));
			} else {
				User newUser = User.builder().firstName(oAuth2User.getAttribute("given_name"))
						.lastName(oAuth2User.getAttribute("family_name")).email(oAuth2User.getAttribute("email"))
						.mention(oAuth2User.getEmail().split("@")[0]).image(oAuth2User.getAttribute("picture"))
						.build();
				user = userRepository.save(newUser);
			}

			String token = jwtTokenProvider.generateToken(user);
			String refresh = jwtTokenProvider.generateRefreshToken(user);
			// taskflow://users/oauth2?token=
			response.sendRedirect("taskflow://users/oauth2?token=" + token + "&refresh=" + refresh);
		}

	}

}
