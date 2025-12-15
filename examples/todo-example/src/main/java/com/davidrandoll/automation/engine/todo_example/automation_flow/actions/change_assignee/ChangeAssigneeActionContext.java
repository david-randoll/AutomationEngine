package com.davidrandoll.automation.engine.todo_example.automation_flow.actions.change_assignee;

import com.davidrandoll.automation.engine.backend.api.json_schema.SchemaDescription;
import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChangeAssigneeActionContext implements IActionContext {
    private String alias;
    private String description;

    @SchemaDescription("The ID of the to-do item to change the assignee for.")
    @JsonAlias({"id", "todoId"})
    private Long todoId;

    @SchemaDescription("The username of the new assignee.")
    private String newAssigneeUsername;

    @SchemaDescription("The full name of the new assignee.")
    private String newAssigneeFullName;

    @SchemaDescription("The variable to store the result in.")
    private String storeToVariable;
}