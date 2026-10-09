package com.companywiseprep.web;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Render's free plan stops a service after 15 minutes without inbound requests, and the next
 * request then waits about a minute for a cold start. When prep.keep-alive.url is set (on Render it
 * defaults to RENDER_EXTERNAL_URL, which Render provides), the app calls its own public URL every
 * few minutes so it counts as active. The call goes out through Render's edge and back in, which is
 * what resets the idle timer; a call to localhost would not. Locally the URL is empty and this does
 * nothing.
 *
 * It pings /api/auth/status, which needs no login and does not touch the database, so Neon can
 * still suspend its compute while nobody is studying.
 */
@Configuration
@EnableScheduling
public class KeepAlive {

	private static final Logger log = LoggerFactory.getLogger(KeepAlive.class);

	private final String url;
	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

	public KeepAlive(@Value("${prep.keep-alive.url:}") String baseUrl,
			@Value("${prep.keep-alive.interval:PT10M}") Duration interval) {
		String base = baseUrl.strip();
		this.url = base.isEmpty() ? "" : base.replaceAll("/+$", "") + "/api/auth/status";
		if (!url.isEmpty()) log.info("Keep-alive: pinging {} every {}", url, interval);
	}

	@Scheduled(fixedDelayString = "${prep.keep-alive.interval:PT10M}", initialDelayString = "${prep.keep-alive.interval:PT10M}")
	void ping() {
		if (url.isEmpty()) return;
		try {
			HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(30)).GET().build();
			int status = http.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
			if (status != 200) log.warn("Keep-alive: {} returned {}", url, status);
		} catch (Exception e) {
			if (e instanceof InterruptedException) Thread.currentThread().interrupt();
			log.warn("Keep-alive: {} failed: {}", url, e.toString());
		}
	}
}
