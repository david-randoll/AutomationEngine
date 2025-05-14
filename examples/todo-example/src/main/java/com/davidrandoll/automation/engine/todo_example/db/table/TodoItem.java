package com.davidrandoll.automation.engine.todo_example.db.table;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "todo_item")
public class TodoItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private TodoStatus status;

    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private TodoAssignee assignee;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public TodoItem(String title, TodoStatus status, TodoAssignee assignee) {
        this.title = title;
        this.status = status;
        this.assignee = assignee;
    }

    private TodoItem() {
        // Default constructor for JPA
    }
}