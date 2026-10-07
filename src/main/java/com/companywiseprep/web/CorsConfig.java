package com.companywiseprep.web;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Hosted split (UI on Netlify, API on Render): allow exactly the configured UI origins
 * (prep.cors.allowed-origins, comma-separated). Empty = same-origin only, as when running locally.
 * Registered as a servlet filter ahead of AuthFilter so preflights and 401s carry CORS headers.
 */
@Configuration
public class CorsConfig {

	@Bean
	FilterRegistrationBean<CorsFilter> corsFilter(@Value("${prep.cors.allowed-origins:}") String origins) {
		List<String> allowed = Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
		CorsConfiguration cors = new CorsConfiguration();
		cors.setAllowedOrigins(allowed);
		cors.setAllowedMethods(List.of("GET", "POST", "PUT", "OPTIONS"));
		cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		cors.setAllowCredentials(false);
		cors.setMaxAge(3600L);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		if (!allowed.isEmpty()) source.registerCorsConfiguration("/api/**", cors);
		FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 5);
		return bean;
	}
}
