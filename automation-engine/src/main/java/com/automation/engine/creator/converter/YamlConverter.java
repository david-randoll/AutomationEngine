package com.automation.engine.creator.converter;

import com.automation.engine.creator.parsers.yaml.IYamlConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class YamlConverter implements IYamlConverter {
    @Override
    public <T> T convert(String yaml, Class<T> clazz) {
        try {
            var mapper = getYamlObjectMapper();
            return mapper.readValue(yaml, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public ObjectMapper getYamlObjectMapper() {
        return Jackson2ObjectMapperBuilder.yaml().build();
    }
}
