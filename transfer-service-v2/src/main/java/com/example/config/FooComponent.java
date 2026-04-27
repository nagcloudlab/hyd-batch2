package com.example.config;

import org.springframework.stereotype.Component;

@Component("fooComponent") // Marks this class as a Spring-managed component with the name 'fooComponent'
public class FooComponent {

    private String foo = "foofoo";

    public String getFoo() {
        return foo;
    }

}
