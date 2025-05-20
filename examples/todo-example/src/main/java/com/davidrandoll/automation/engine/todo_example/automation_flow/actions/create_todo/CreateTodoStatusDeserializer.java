package com.davidrandoll.automation.engine.todo_example.automation_flow.actions.create_todo;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.ObjectUtils;

import java.io.IOException;

public class CreateTodoStatusDeserializer extends JsonDeserializer<CreateTodoActionContext.CreateTodoStatus> {
    @Override
    public CreateTodoActionContext.CreateTodoStatus deserialize(JsonParser p, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode node = p.readValueAsTree();
        if (node.isTextual()) {
            String text = node.asText().trim();
            var result = new CreateTodoActionContext.CreateTodoStatus();
            if (!ObjectUtils.isEmpty(text)) {
                result.setCode(text);
            }
            return result;
        } else {
            var objectMapper = p.getCodec();
            return objectMapper.treeToValue(node, CreateTodoActionContext.CreateTodoStatus.class);
        }
    }
}