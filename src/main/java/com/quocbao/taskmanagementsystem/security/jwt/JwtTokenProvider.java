package com.quocbao.taskmanagementsystem.security.jwt;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.quocbao.taskmanagementsystem.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtTokenProvider {

	@Value("${application.security.jwt.secret-key}")
	private String jwtSercret;

	@Value("${application.security.jwt.expiration}")
	private long jwtExp;

	@Value("${application.security.jwt.refresh-token.expiration}")
	private long jwtRefresh;

	private SecretKey getSignInKey() {
		byte[] keyBytes = Decoders.BASE64URL.decode(jwtSercret);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateToken(User user) {
		return Jwts.builder().subject(user.getEmail()).issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + jwtExp)).signWith(getSignInKey(), Jwts.SIG.HS256)
				.compact();

	}

	private Claims extractPayload(String token) {
		return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
	}

	public String extractEmail(String token) {
		return extractPayload(token).getSubject();
	}

	public Boolean isTokenValid(String token, User user) {
		return (extractEmail(token).equals(user.getEmail()) && !isTokenExpiration(token));
	}

	private Boolean isTokenExpiration(String token) {
		return extractPayload(token).getExpiration().before(new Date());
	}

	public String generateRefreshToken(User user) {
		return Jwts.builder().subject(user.getEmail()).issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + jwtRefresh)).signWith(getSignInKey(), Jwts.SIG.HS256)
				.compact();
	}

	public Boolean validationToken(String token) {
		try {
			Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token);
			return true;
		} catch (ExpiredJwtException ex) {
			throw ex;
		} catch (MalformedJwtException ex) {
			throw ex;
		} catch (SignatureException ex) {
			throw ex;
		} catch (UnsupportedJwtException ex) {
			throw ex;
		} catch (IllegalArgumentException ex) {
			throw ex;
		} catch (JwtException ex) {
			throw ex;
		}
	}
}
