package com.companywiseprep;

import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CompanyWisePrepApplication {

	public static void main(String[] args) {
		SpringApplication.run(CompanyWisePrepApplication.class, args);
	}

	/** Injected wherever "today" matters, so tests can pin the date. */
	@Bean
	Clock clock() {
		return Clock.systemDefaultZone();
	}
}
