package com.companywiseprep.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

class KeepAliveTest {

	@Test
	void pingsStatusEndpointOfConfiguredUrl() throws Exception {
		List<String> paths = new CopyOnWriteArrayList<>();
		HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/", exchange -> {
			paths.add(exchange.getRequestURI().getPath());
			exchange.sendResponseHeaders(200, -1);
			exchange.close();
		});
		server.start();
		try {
			String base = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
			new KeepAlive(base, Duration.ofMinutes(10)).ping();
			assertThat(paths).containsExactly("/api/auth/status");
		} finally {
			server.stop(0);
		}
	}

	@Test
	void doesNothingWithoutUrl() {
		new KeepAlive("  ", Duration.ofMinutes(10)).ping(); // no request, no exception
	}
}
