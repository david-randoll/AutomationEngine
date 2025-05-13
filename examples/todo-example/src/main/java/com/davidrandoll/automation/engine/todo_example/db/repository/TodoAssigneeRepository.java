package com.davidrandoll.automation.engine.todo_example.db.repository;


import com.davidrandoll.automation.engine.todo_example.db.table.TodoAssignee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TodoAssigneeRepository extends JpaRepository<TodoAssignee, Long> {
    Optional<TodoAssignee> findByUsername(String username);
}