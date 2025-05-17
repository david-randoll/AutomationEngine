package com.davidrandoll.automation.engine.todo_example.automation_flow.actions.create_todo;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
public class CreateTodoActionContext implements IActionContext {
    private String alias;
    private String title;
    private String storeToVariable = "todo";

    @JsonAlias({"status", "todoStatus"})
    @JsonDeserialize(using = CreateTodoStatusDeserializer.class)
    private CreateTodoStatus todoStatus = new CreateTodoStatus();

    @JsonAlias({"assignee", "todoAssignee"})
    @JsonDeserialize(using = CreateTodoAssigneeDeserializer.class)
    private CreateTodoAssignee todoAssignee = new CreateTodoAssignee();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class CreateTodoStatus {
        private Long id;
        private String code = "pending";
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class CreateTodoAssignee {
        private Long id;
        @JsonAlias({"username", "userName", "email"})
        private String username;
        private String fullName;
    }
}