package com.automation.engine.core.utils;

import lombok.experimental.UtilityClass;
import org.springframework.lang.NonNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@UtilityClass
public class GenericTypeResolver {
    public static Class<?> getGenericParameterClass(@NonNull Class<?> clazz) {
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
