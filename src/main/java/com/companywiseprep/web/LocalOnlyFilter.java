package com.companywiseprep.web;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

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
 * This app runs the learner's Java code and has no sign-in, so the API must only answer pages
 * served from this machine. Binding to 127.0.0.1 stops other computers; this filter stops other
 * *websites* open in the same browser:
 * <ul>
 * <li>Host must be localhost/127.0.0.1 — defeats DNS rebinding (evil.com resolving to 127.0.0.1).</li>
 * <li>A cross-origin Origin header is refused on anything that changes state or runs code.</li>
 * </ul>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LocalOnlyFilter extends OncePerRequestFilter {

	private static final Set<String> LOCAL_HOSTS = Set.of("localhost", "127.0.0.1", "[::1]");
	private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");

	/** Hosted (prep.auth.enabled): the password login + CORS allowlist protect the API instead. */
	private final boolean hosted;

	public LocalOnlyFilter(@org.springframework.beans.factory.annotation.Value("${prep.auth.enabled:false}") boolean hosted) {
		this.hosted = hosted;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return hosted || !request.getRequestURI().startsWith("/api/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String host = request.getHeader("Host") != null ? hostOnly(request.getHeader("Host")) : request.getServerName();
		if (!isLocal(host)) {
			response.sendError(HttpStatus.FORBIDDEN.value(), "This app only answers requests addressed to localhost");
			return;
		}
		String origin = request.getHeader("Origin");
		if (!SAFE_METHODS.contains(request.getMethod()) && origin != null && !isLocal(originHost(origin))) {
			response.sendError(HttpStatus.FORBIDDEN.value(), "Cross-site requests are not allowed");
			return;
		}
		chain.doFilter(request, response);
	}

	private static boolean isLocal(String host) {
		return host != null && LOCAL_HOSTS.contains(host.toLowerCase());
	}

	/** "localhost:8090" → "localhost"; "[::1]:8090" → "[::1]". */
	static String hostOnly(String hostHeader) {
		if (hostHeader == null) return null;
		if (hostHeader.startsWith("[")) {
			int end = hostHeader.indexOf(']');
			return end > 0 ? hostHeader.substring(0, end + 1) : hostHeader;
		}
		int colon = hostHeader.indexOf(':');
		return colon >= 0 ? hostHeader.substring(0, colon) : hostHeader;
	}

	private static String originHost(String origin) {
		try {
			String host = URI.create(origin).getHost();
			return host != null && host.contains(":") && !host.startsWith("[") ? "[" + host + "]" : host;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
