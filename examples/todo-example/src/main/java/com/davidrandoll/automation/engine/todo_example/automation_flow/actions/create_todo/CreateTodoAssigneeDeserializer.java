package com.davidrandoll.automation.engine.todo_example.automation_flow.actions.create_todo;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class CreateTodoAssigneeDeserializer extends JsonDeserializer<CreateTodoActionContext.CreateTodoAssignee> {
    @Override
    public CreateTodoActionContext.CreateTodoAssignee deserialize(JsonParser p, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode node = p.readValueAsTree();
        if (node.isTextual()) {
            String text = node.asText().trim();
            return new CreateTodoActionContext.CreateTodoAssignee()
                    .setUsername(text);
        } else {
            return p.readValueAs(CreateTodoActionContext.CreateTodoAssignee.class);
        }
    }
}
