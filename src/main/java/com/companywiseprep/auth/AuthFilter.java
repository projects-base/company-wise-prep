package com.companywiseprep.auth;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * When the app is hosted (prep.auth.enabled), every /api call except the login endpoints needs
 * {@code Authorization: Bearer <token>}. Runs after the CORS filter, so even a 401 carries CORS
 * headers and the browser can show a login screen instead of a CORS error.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AuthFilter extends OncePerRequestFilter {

	private final AuthService auth;

	public AuthFilter(AuthService auth) {
		this.auth = auth;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String uri = request.getRequestURI();
		return !auth.enabled() || !uri.startsWith("/api/") || uri.startsWith("/api/auth/")
				|| "OPTIONS".equals(request.getMethod());
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String header = request.getHeader("Authorization");
		String token = header != null && header.startsWith("Bearer ") ? header.substring(7).trim() : null;
		if (!auth.valid(token)) {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			response.setContentType("application/json");
			response.getWriter().write("{\"error\":\"login required\"}");
			return;
		}
		chain.doFilter(request, response);
	}
}
