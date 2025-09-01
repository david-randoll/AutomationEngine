package com.davidrandoll.automation.engine.spring.converter;

import com.davidrandoll.automation.engine.creator.parsers.yaml.IYamlConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

public class YamlConverter implements IYamlConverter {
    @Override
    public <T> T convert(String yaml, Class<T> clazz) {
        try {
            var mapper = getYamlObjectMapper();
            return mapper.readValue(yaml, clazz);
        } catch (JsonProcessingException e) {
            throw new AutomationEngineInvalidYamlException(e);
        }
    }

    public ObjectMapper getYamlObjectMapper() {
        return Jackson2ObjectMapperBuilder
                .yaml()
                .build();
    }

    public static class AutomationEngineInvalidYamlException extends RuntimeException {
        public AutomationEngineInvalidYamlException(Throwable cause) {
            super(cause);
        }
    }
}
