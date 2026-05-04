package com.example;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class GreetService {

    @Cacheable(value = "greet-cache", key = "#name", cacheManager = "redisCacheManager")
    public String greet(String name) {
        System.out.println("greet() called with " + name);
        return "Hello " + name;
    }

    // @Transactional
    public void m() {
        // db-call
        // api-call to service-A // 5 s
    }

}
