package com.davidrandoll.automation.engine.test;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.experimental.UtilityClass;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@UtilityClass
public class JsonTestUtils {
    public static JsonNode json(String json) {
        var mapper = Jackson2ObjectMapperBuilder.json()
                .build();
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }
}
