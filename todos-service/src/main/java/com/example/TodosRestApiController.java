package com.example;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;

@RestController
public class TodosRestApiController {

    private List<Todo> todos = new ArrayList<>();

    private Todo createTodo(Long id, String title, boolean completed, TodoCategory category) {
        return new Todo(id, title, completed, category);
    }

    @PostConstruct
    public void init() {
        todos.add(createTodo(1L, "Buy groceries", false, TodoCategory.PERSONAL));
        todos.add(createTodo(2L, "Finish project report", true, TodoCategory.WORK));
        todos.add(createTodo(3L, "Watch a movie", false, TodoCategory.ENTERTAINMENT));
        todos.add(createTodo(4L, "Go for a run", true, TodoCategory.HEALTH));
    }

    // ------------------------------------------------------------------
    // Read endpoints
    // ------------------------------------------------------------------

    // How to map request to method:
    // by URL
    // by HTTP method (GET, POST, PUT, DELETE)
    // by content type (application/json, application/xml, etc.)
    // by presence or absence of query parameters, headers, etc.

    // GET /api/v1/todos

    @RequestMapping(value = "/api/v1/todos", params = {
            "!cat" }, headers = { "!language" }, method = RequestMethod.GET, produces = "application/json")
    public List<Todo> getAllTodos() {
        return todos;
    }

    @RequestMapping(value = "/api/v1/todos", params = {
            "cat" }, method = RequestMethod.GET, produces = "application/json")
    public List<Todo> getTodosByCat(@RequestParam("cat") String cat) {
        TodoCategory category;
        try {
            category = TodoCategory.valueOf(cat.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category: " + cat);
        }
        return todos.stream()
                .filter(todo -> todo.getCategory() == category)
                .toList();
    }

    @RequestMapping(value = "/api/v1/todos", params = {
            "cat=work" }, method = RequestMethod.GET, produces = "application/json")
    public List<Todo> getWorkTodos() {
        System.out.println("getWorkTodos() called");
        return todos.stream()
                .filter(todo -> todo.getCategory() == TodoCategory.WORK)
                .toList();
    }

    // GET /api/v1/todos/{id}
    @RequestMapping(value = "/api/v1/todos/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getTodoById(@PathVariable("id") Long id) {
        Todo foundTodo = todos.stream()
                .filter(todo -> todo.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (foundTodo == null) {
            throw new TodoNotFoundException(id);
        }
        return ResponseEntity.ok(foundTodo);

    }

    // HEAD /api/v1/todos/{id}
    @RequestMapping(value = "/api/v1/todos/{id}", method = RequestMethod.HEAD)
    public ResponseEntity<?> checkTodoExists(@PathVariable("id") Long id) {
        boolean exists = todos.stream()
                .anyMatch(todo -> todo.getId().equals(id));
        if (exists) {
            return ResponseEntity.ok().build();
        } else {
            throw new TodoNotFoundException(id);
        }
    }

    // OPTIONS /api/v1/todos
    @RequestMapping(value = "/api/v1/todos", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> getTodosOptions() {
        // check What Privileges the client has on this resource
        // Return as Access-Control-Allow-Methods header
        return ResponseEntity.ok()
                .header("Allow", "GET", "HEAD", "OPTIONS")
                .build();
    }

    // ----------------------------------------------------------------
    // Write endpoints
    // ----------------------------------------------------------------

    @RequestMapping(value = "/api/v1/todos", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createTodo(@RequestBody CreateTodoDto createTodoDto) {
        Long newId = todos.stream()
                .mapToLong(Todo::getId)
                .max()
                .orElse(0L) + 1;
        Todo newTodo = createTodo(newId, createTodoDto.getTitle(), createTodoDto.isCompleted(),
                TodoCategory.valueOf(createTodoDto.getCategory().toUpperCase()));
        todos.add(newTodo);
        // return 201 Created with Location header pointing to the new resource
        return ResponseEntity.created(URI.create("/api/v1/todos/" + newTodo.getId()))
                .body(newTodo);
    }

    // PUT /api/v1/todos/{id}
    @RequestMapping(value = "/api/v1/todos/{id}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateTodo(@PathVariable("id") Long id, @RequestBody UpdateTodoDto updateTodoDto) {
        Todo existingTodo = todos.stream()
                .filter(todo -> todo.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (existingTodo == null) {
            return ResponseEntity.status(404).body("Todo with id " + id + " not found");
        }
        existingTodo.setTitle(updateTodoDto.getTitle());
        existingTodo.setCompleted(updateTodoDto.isCompleted());
        existingTodo.setCategory(TodoCategory.valueOf(updateTodoDto.getCategory().toUpperCase()));
        return ResponseEntity.ok(existingTodo);
    }

    // DELETE /api/v1/todos/{id}
    @RequestMapping(value = "/api/v1/todos/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteTodo(@PathVariable("id") Long id) {
        boolean removed = todos.removeIf(todo -> todo.getId().equals(id));
        if (removed) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(404).body("Todo with id " + id + " not found");
        }
    }

    //

}
