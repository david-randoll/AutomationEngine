package com.davidrandoll.automation.engine.core.utils;

import lombok.experimental.UtilityClass;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@UtilityClass
public class GenericTypeResolver {
    public static Class<?> getGenericParameterClass(Class<?> clazz) {
        if (clazz == null)
            throw new IllegalArgumentException("Class cannot be null");

        try {
            Type superClass = clazz.getGenericSuperclass();
            if (superClass instanceof ParameterizedType parameterizedType) {
                Type type = parameterizedType.getActualTypeArguments()[0];
                if (type instanceof Class<?> parameterClass) {
                    return parameterClass;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to determine generic parameter type", e);
        }
        throw new RuntimeException("Cannot determine generic parameter type");
    }
}
