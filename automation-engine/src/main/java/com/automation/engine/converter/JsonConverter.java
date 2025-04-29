package com.automation.engine.converter;

import com.automation.engine.conditional.AEConditionalOnMissingBeanType;
import com.automation.engine.creator.parsers.json.IJsonConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@AEConditionalOnMissingBeanType(IJsonConverter.class)
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