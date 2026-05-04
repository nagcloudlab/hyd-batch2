package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@SpringBootApplication
@EnableCaching
public class PlayWithSpringBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlayWithSpringBootApplication.class, args);
	}

	@Bean
	public CacheManager redisCacheManager(RedisConnectionFactory factory) {
		return RedisCacheManager.builder(factory).build();
	}

}
