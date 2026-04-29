package com.example.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.dto.CreateTodoDto;
import com.example.entity.Todo;

@Service
public class TodoService {

    private final com.example.repository.TodoRepository todoRepository;
    private final com.example.repository.UserRepository userRepository;

    public TodoService(com.example.repository.TodoRepository todoRepository,
            com.example.repository.UserRepository userRepository) {
        this.todoRepository = todoRepository;
        this.userRepository = userRepository;
    }

    // @PreAuthorize("hasRole('ADMIN')") // Only users with ROLE_ADMIN can create
    // todos
    public void createTodo(CreateTodoDto createTodoDto) {
        Todo todo = new Todo();
        todo.setTitle(createTodoDto.getTitle());
        todo.setDescription(createTodoDto.getDescription());

        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication();
        String username = auth.getName();
        com.example.entity.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        todo.setUser(user);

        todoRepository.save(todo);
    }

}
