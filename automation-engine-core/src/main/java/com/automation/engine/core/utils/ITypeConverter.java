package com.automation.engine.core.utils;

public interface ITypeConverter {
    <T> T convert(Object object, Class<?> clazz);
}