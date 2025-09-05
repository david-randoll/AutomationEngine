package com.davidrandoll.automation.engine.spring.spi;

import com.fasterxml.jackson.core.type.TypeReference;

public interface ITypeConverter {
    <T> T convert(Object object, Class<?> clazz);
    <T> T convert(Object object, TypeReference<T> typeReference);
}