package com.davidrandoll.automation.engine.backend.api.json_schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JsonSchemaService {
    private final JsonSchemaGenerator jsonSchemaGenerator;

    public JsonNode generateSchema(Class<?> clazz) {
        return jsonSchemaGenerator.generateJsonSchema(clazz);
    }
}