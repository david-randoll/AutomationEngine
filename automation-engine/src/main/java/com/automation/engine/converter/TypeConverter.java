package com.automation.engine.converter;

import com.automation.engine.spi.ITypeConverter;
import com.automation.engine.core.utils.InvalidInputException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TypeConverter implements ITypeConverter {
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T convert(Object object, Class<?> clazz) {
        try {
            var dataStr = objectMapper.writeValueAsString(object);
            return (T) objectMapper.readValue(dataStr, clazz);
        } catch (MismatchedInputException e) {
            throw new InvalidInputException(e);
        }
    }
}