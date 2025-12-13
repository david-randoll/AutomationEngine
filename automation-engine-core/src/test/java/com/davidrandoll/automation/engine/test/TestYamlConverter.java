package com.davidrandoll.automation.engine.test;

import com.davidrandoll.automation.engine.creator.parsers.yaml.IYamlConverter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class TestYamlConverter implements IYamlConverter {
    private final ObjectMapper mapper;

    public TestYamlConverter() {
        this.mapper = new ObjectMapper(new YAMLFactory());
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public <T> T convert(String yaml, Class<T> clazz) {
        try {
            return mapper.readValue(yaml, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse YAML", e);
        }
    }
}
