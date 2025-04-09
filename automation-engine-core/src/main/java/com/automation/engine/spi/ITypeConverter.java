package com.automation.engine.spi;

public interface ITypeConverter {
    <T> T convert(Object object, Class<?> clazz);
}