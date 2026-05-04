package com.example.demo;

import java.time.Duration;
import java.util.function.Supplier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
public class DemoApplication {

	@GetMapping("/hello")
	public Mono<String> hello() {
		// thread who received the request will be released immediately, and the
		// response will be sent after 3 seconds
		System.out.println("Received request on thread: " + Thread.currentThread().getName());
		return Mono
				.delay(Duration.ofSeconds(3))
				.map(ignore -> "Hello, World!");
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
