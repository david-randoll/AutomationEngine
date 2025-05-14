package com.davidrandoll.automation.engine.todo_example.db.table;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "todo_assignee")
public class TodoAssignee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String fullName;

    public TodoAssignee(String username, String fullName) {
        this.username = username;
        this.fullName = fullName;
    }

    private TodoAssignee() {
        // Default constructor for JPA
    }
}