package com.ratelimiter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RatelimiterApplication {

	public static void main(String[] args) {
		SpringApplication.run(RatelimiterApplication.class, args);
//		System.out.println("Connecting to Redis at: " + System.getenv("SPRING_REDIS_HOST") + ":" + System.getenv("SPRING_REDIS_PORT"));

	}

}
