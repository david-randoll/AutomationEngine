package com.davidrandoll.automation.engine.http.jackson.flexible_method;

import com.davidrandoll.spring_web_captor.event.HttpMethodEnum;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FlexibleHttpMethodListDeserializer extends JsonDeserializer<List<HttpMethodEnum>> {

    @Override
    public List<HttpMethodEnum> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.readValueAsTree();

        if (node.isTextual()) {
            return List.of(HttpMethodEnum.fromValue(node.asText()));
        }

        if (node.isArray()) {
            List<HttpMethodEnum> methods = new ArrayList<>();
            for (JsonNode methodNode : node) {
                methods.add(HttpMethodEnum.fromValue(methodNode.asText()));
            }
            return methods;
        }

        throw new IllegalArgumentException("Unsupported value type for methods: " + node.getNodeType());
    }
}