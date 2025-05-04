package com.davidrandoll.automation.engine.spi;

public interface ITypeConverter {
    <T> T convert(Object object, Class<?> clazz);
}