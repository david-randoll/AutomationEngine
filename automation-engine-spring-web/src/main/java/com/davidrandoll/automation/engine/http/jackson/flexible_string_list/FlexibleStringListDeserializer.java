package com.davidrandoll.automation.engine.http.jackson.flexible_string_list;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class FlexibleStringListDeserializer extends JsonDeserializer<List<String>> implements ContextualDeserializer {

    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.readValueAsTree();

        if (node.isTextual()) {
            return transform(List.of(node.textValue()));
        } else if (node.isArray()) {
            List<String> values = new ArrayList<>();
            for (JsonNode item : node) {
                values.add(item.asText());
            }
            return transform(values);
        }

        throw new IllegalArgumentException("Unsupported value type for FlexibleStringList: " + node.getNodeType());
    }

    private List<String> transform(List<String> input) {
        return input.stream()
                .map(s -> s.replaceAll("\\{[^}]+}", ".*"))
                .map(s -> s.replaceAll("/$", ""))
                .toList();
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        FlexibleStringList annotation = property.getAnnotation(FlexibleStringList.class);
        if (annotation != null) {
            return new FlexibleStringListDeserializer();
        }
        return this;
    }
}
