package com.davidrandoll.automation.engine.todo_example.db.repository;

import com.davidrandoll.automation.engine.todo_example.db.table.TodoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoItemRepository extends JpaRepository<TodoItem, Long> {
}