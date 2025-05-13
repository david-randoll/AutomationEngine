package com.davidrandoll.automation.engine.todo_example.db.table;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "todo_status")
public class TodoStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code; // e.g., "OPEN", "IN_PROGRESS", "DONE"
    private String description;
}