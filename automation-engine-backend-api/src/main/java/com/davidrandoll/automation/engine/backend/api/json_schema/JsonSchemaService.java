package com.davidrandoll.automation.engine.backend.api.json_schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JsonSchemaService {
    private final SchemaGenerator jsonSchemaGenerator;

    public JsonNode generateSchema(Class<?> clazz) {
        return jsonSchemaGenerator.generateSchema(clazz);
    }
}