package com.davidrandoll.automation.engine.todo_example.automation_flow.actions.create_todo;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.ObjectUtils;

import java.io.IOException;

public class CreateTodoAssigneeDeserializer extends JsonDeserializer<CreateTodoActionContext.CreateTodoAssignee> {
    @Override
    public CreateTodoActionContext.CreateTodoAssignee deserialize(JsonParser p, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = p.readValueAsTree();
        if (node.isTextual()) {
            String text = node.asText().trim();
            var result = new CreateTodoActionContext.CreateTodoAssignee();
            if (!ObjectUtils.isEmpty(text)) {
                result.setUsername(text);
            }
            return result;
        } else {
            var objectMapper = p.getCodec();
            return objectMapper.treeToValue(node, CreateTodoActionContext.CreateTodoAssignee.class);
        }
    }
}
