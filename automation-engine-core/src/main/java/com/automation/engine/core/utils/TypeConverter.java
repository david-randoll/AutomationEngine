package com.automation.engine.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("unchecked")
public class TypeConverter {
    private final ObjectMapper objectMapper;

    public TypeConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    public <T> T convert(Object object, Class<?> clazz) {
        try {
            var dataStr = objectMapper.writeValueAsString(object);
            return (T) objectMapper.readValue(dataStr, clazz);
        } catch (MismatchedInputException e) {
            throw new InvalidInputException(e);
        }
    }
}