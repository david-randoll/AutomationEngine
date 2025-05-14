package com.davidrandoll.automation.engine.todo_example.manual_flow;

import com.davidrandoll.automation.engine.todo_example.db.table.TodoItem;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos/manual")
@RequiredArgsConstructor
public class TodoController {
    private final TodoService todoService;

    @PostMapping
    public ResponseEntity<TodoItem> create(@RequestParam String title,
                                           @RequestParam String statusCode,
                                           @RequestParam(required = false) String assigneeUsername) {
        return ResponseEntity.ok(todoService.create(title, statusCode, assigneeUsername));
    }

    @GetMapping
    public ResponseEntity<List<TodoItem>> getAll() {
        return ResponseEntity.ok(todoService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoItem> getById(@PathVariable Long id) {
        return ResponseEntity.ok(todoService.getById(id));
    }

    @PutMapping("/{id}/title")
    public ResponseEntity<TodoItem> updateTitle(@PathVariable Long id, @RequestParam String title) {
        return ResponseEntity.ok(todoService.updateTitle(id, title));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TodoItem> changeStatus(@PathVariable Long id, @RequestParam String statusCode) {
        return ResponseEntity.ok(todoService.changeStatus(id, statusCode));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<TodoItem> assignUser(@PathVariable Long id, @RequestParam String username) {
        return ResponseEntity.ok(todoService.assignUser(id, username));
    }

    @PutMapping("/{id}/unassign")
    public ResponseEntity<TodoItem> unassignUser(@PathVariable Long id) {
        return ResponseEntity.ok(todoService.unassignUser(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        todoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}