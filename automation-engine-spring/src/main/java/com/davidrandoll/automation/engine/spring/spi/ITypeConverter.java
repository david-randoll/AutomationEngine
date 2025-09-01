package com.davidrandoll.automation.engine.spring.spi;

public interface ITypeConverter {
    <T> T convert(Object object, Class<?> clazz);
}