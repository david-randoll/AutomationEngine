package com.davidrandoll.automation.engine.spring.converter;

import com.davidrandoll.automation.engine.spring.spi.ITypeConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TypeConverter implements ITypeConverter {
    private final ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    public <T> T convert(Object object, Class<?> clazz) {
        var result = (T) objectMapper.convertValue(object, clazz);
        if (result == null) {
            log.warn("Type conversion failed. Object: {}, Class: {}", object, clazz);
        }
        return result;
    }
}