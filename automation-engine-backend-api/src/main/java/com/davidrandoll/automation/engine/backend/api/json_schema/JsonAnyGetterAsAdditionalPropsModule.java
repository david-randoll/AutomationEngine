package com.davidrandoll.automation.engine.backend.api.json_schema;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.stream.Stream;

public class JsonAnyGetterAsAdditionalPropsModule implements Module {
    @Override
    public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
        builder.forTypesInGeneral()
                .withAdditionalPropertiesResolver((scope, context) -> {
                    Class<?> clazz = scope.getType().getErasedType();
                    // Find field or method with @JsonAnySetter
                    var jsonAnySetterElement = Stream.concat(
                                    Arrays.stream(clazz.getDeclaredFields()),
                                    Arrays.stream(clazz.getDeclaredMethods()))
                            .filter(el -> el.isAnnotationPresent(JsonAnySetter.class))
                            .findFirst();

                    if (jsonAnySetterElement.isPresent()) {
                        Class<?> valueType = Object.class;

                        AnnotatedElement el = jsonAnySetterElement.get();
                        if (el instanceof Field field) {
                            valueType = extractValueType(field.getGenericType());
                        } else if (el instanceof Method method) {
                            Type[] paramTypes = method.getGenericParameterTypes();
                            if (paramTypes.length == 2) {
                                valueType = extractValueType(paramTypes[1]);
                            }
                        }

                        if (JsonNode.class.isAssignableFrom(valueType)) {
                            valueType = Object.class;
                        }

                        var resolvedType = context.getTypeContext().resolve(valueType);
                        return context.createDefinitionReference(resolvedType);
                    }
                    return null;
                });
    }

    private Class<?> extractValueType(Type type) {
        if (type instanceof ParameterizedType pt) {
            Type[] actualTypeArguments = pt.getActualTypeArguments();
            if (actualTypeArguments.length == 2) {
                Type valueType = actualTypeArguments[1];
                if (valueType instanceof Class<?> clazz) {
                    return clazz;
                } else if (valueType instanceof ParameterizedType param) {
                    Type raw = param.getRawType();
                    if (raw instanceof Class<?> rawClass) {
                        return rawClass;
                    }
                }
            }
        } else if (type instanceof Class<?> clazz) {
            return clazz;
        }

        return Object.class;
    }
}
