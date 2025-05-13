package com.davidrandoll.automation.engine.todo_example.db.repository;

import com.davidrandoll.automation.engine.todo_example.db.table.TodoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TodoStatusRepository extends JpaRepository<TodoStatus, Long> {
    Optional<TodoStatus> findByCode(String code);
}