package com.davidrandoll.automation.engine.todo_example.manual_flow;

import com.davidrandoll.automation.engine.todo_example.db.repository.TodoAssigneeRepository;
import com.davidrandoll.automation.engine.todo_example.db.repository.TodoItemRepository;
import com.davidrandoll.automation.engine.todo_example.db.repository.TodoStatusRepository;
import com.davidrandoll.automation.engine.todo_example.db.table.TodoAssignee;
import com.davidrandoll.automation.engine.todo_example.db.table.TodoItem;
import com.davidrandoll.automation.engine.todo_example.db.table.TodoStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class TodoService {
    private final TodoItemRepository todoItemRepository;
    private final TodoStatusRepository todoStatusRepository;
    private final TodoAssigneeRepository todoAssigneeRepository;

    public TodoItem create(String title, String statusCode, String assigneeUsername) {
        TodoStatus status = todoStatusRepository.findByCode(statusCode)
                .orElseGet(() -> {
                    TodoStatus newStatus = new TodoStatus(statusCode, "Auto-created: " + statusCode);
                    return todoStatusRepository.save(newStatus);
                });

        TodoAssignee assignee = null;
        if (assigneeUsername != null) {
            assignee = todoAssigneeRepository.findByUsername(assigneeUsername)
                    .orElseGet(() -> {
                        TodoAssignee newAssignee = new TodoAssignee(assigneeUsername, assigneeUsername);
                        return todoAssigneeRepository.save(newAssignee);
                    });
        }

        TodoItem item = new TodoItem(title, status, assignee);
        return todoItemRepository.save(item);
    }

    public List<TodoItem> getAll() {
        return todoItemRepository.findAll();
    }

    public TodoItem getById(Long id) {
        return todoItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TodoItem not found: " + id));
    }

    public TodoItem updateTitle(Long id, String newTitle) {
        TodoItem item = getById(id);
        item.setTitle(newTitle);
        return todoItemRepository.save(item);
    }

    public TodoItem changeStatus(Long id, String newStatusCode) {
        TodoItem item = getById(id);
        TodoStatus status = todoStatusRepository.findByCode(newStatusCode)
                .orElseGet(() -> {
                    TodoStatus newStatus = new TodoStatus(newStatusCode, "Auto-created: " + newStatusCode);
                    return todoStatusRepository.save(newStatus);
                });
        item.setStatus(status);
        return todoItemRepository.save(item);
    }

    public TodoItem assignUser(Long id, String username) {
        TodoItem item = getById(id);
        TodoAssignee user = todoAssigneeRepository.findByUsername(username)
                .orElseGet(() -> {
                    TodoAssignee newAssignee = new TodoAssignee(username, username);
                    return todoAssigneeRepository.save(newAssignee);
                });
        item.setAssignee(user);
        return todoItemRepository.save(item);
    }

    public TodoItem unassignUser(Long id) {
        TodoItem item = getById(id);
        item.setAssignee(null);
        return todoItemRepository.save(item);
    }

    public void delete(Long id) {
        todoItemRepository.deleteById(id);
    }
}