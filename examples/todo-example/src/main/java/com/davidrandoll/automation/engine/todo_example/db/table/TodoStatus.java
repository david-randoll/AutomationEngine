package com.davidrandoll.automation.engine.todo_example.db.table;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "todo_status")
public class TodoStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code; // e.g., "OPEN", "IN_PROGRESS", "DONE"
    private String description;

    public TodoStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    private TodoStatus() {
        // Default constructor for JPA
    }
}