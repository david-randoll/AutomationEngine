package com.davidrandoll.automation.engine.todo_example.automation_flow.actions.change_status;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChangeStatusActionContext implements IActionContext {
    private String alias;

    @JsonAlias({"id", "todoId"})
    private Long todoId;

    @JsonAlias({"status", "newStatus", "newStatusCode", "statusCode", "code"})
    private String newStatusCode;
    private String storeToVariable;
}