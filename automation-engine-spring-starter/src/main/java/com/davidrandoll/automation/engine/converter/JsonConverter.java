package com.davidrandoll.automation.engine.converter;

import com.davidrandoll.automation.engine.creator.parsers.json.IJsonConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(value = IJsonConverter.class, ignored = JsonConverter.class)
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