package com.example.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final com.example.service.TodoService todoService;

    public TodoController(com.example.service.TodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping
    public ResponseEntity<?> createTodo(@RequestBody com.example.dto.CreateTodoDto createTodoDto) {
        todoService.createTodo(createTodoDto);
        return ResponseEntity.status(201).build();
    }

}
