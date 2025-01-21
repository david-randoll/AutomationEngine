package com.automation.engine.engine.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("unchecked")
public class TypeConverter {
    private final ObjectMapper objectMapper;

    public TypeConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public TypeConverter() {
        this.objectMapper = new ObjectMapper();
    }

    @SneakyThrows
    public <T> T convert(Object object, Class<?> clazz) {
        var dataStr = objectMapper.writeValueAsString(object);
        return (T) objectMapper.readValue(dataStr, clazz);
    }
}