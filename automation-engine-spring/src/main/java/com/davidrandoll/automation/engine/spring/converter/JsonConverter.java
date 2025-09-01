package com.davidrandoll.automation.engine.spring.converter;

import com.davidrandoll.automation.engine.creator.parsers.json.IJsonConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JsonConverter implements IJsonConverter {
    private final ObjectMapper mapper;

    @Override
    public <T> T convert(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}