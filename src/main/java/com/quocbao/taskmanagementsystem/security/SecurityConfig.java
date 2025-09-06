package com.quocbao.taskmanagementsystem.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quocbao.taskmanagementsystem.common.ErrorResponse;
import com.quocbao.taskmanagementsystem.security.jwt.JwtAuthenticationFilter;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	SecurityConfig(CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler,
			JwtAuthenticationFilter jwtAuthenticationFilter) {

		this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;

		this.jwtAuthenticationFilter = jwtAuthenticationFilter;

	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.cors(co -> co.disable());
		httpSecurity.csrf(cf -> cf.disable());
		httpSecurity.anonymous(t -> t.disable());
		httpSecurity.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
				.requestMatchers("/users/oauth2").permitAll()
				.requestMatchers("/users/refresh-token").permitAll()
				.requestMatchers("/users/tests").permitAll()
				.anyRequest().authenticated()

		);

		httpSecurity.oauth2Login(t -> t.successHandler(customAuthenticationSuccessHandler));
		httpSecurity.sessionManagement(
				sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		httpSecurity.exceptionHandling(t -> t.authenticationEntryPoint(authenticationEntryPoint()));
		httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return httpSecurity.build();
	}

	@Bean
	AuthenticationEntryPoint authenticationEntryPoint() {
		return (_, response, _) -> {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			ErrorResponse errorResponse = new ErrorResponse();
			errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			errorResponse.setTitle("ERROR AUTHENTICATE");
			errorResponse.setDetail("Access Denied: Unauthorized Access - Invalid or Expired Token");
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonResponse = objectMapper.writeValueAsString(errorResponse);
			response.getWriter().write(jsonResponse);
		};
	}

}
