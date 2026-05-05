package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class TimeServiceApplication {

	@GetMapping("/time")
	public String getCurrentTime() {
		return "Current time: " + java.time.LocalTime.now().toString();
	}

	public static void main(String[] args) {
		SpringApplication.run(TimeServiceApplication.class, args);
	}

}
