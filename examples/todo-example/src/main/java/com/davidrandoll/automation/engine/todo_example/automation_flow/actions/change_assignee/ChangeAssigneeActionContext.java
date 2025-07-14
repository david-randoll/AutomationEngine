package com.davidrandoll.automation.engine.todo_example.automation_flow.actions.change_assignee;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChangeAssigneeActionContext implements IActionContext {
    private String alias;
    private String description;

    @JsonAlias({"id", "todoId"})
    private Long todoId;
    
    private String newAssigneeUsername;
    private String newAssigneeFullName;
    private String storeToVariable;
}