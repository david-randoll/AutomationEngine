package com.davidrandoll.automation.engine.backend.api.json_schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to provide a description for fields that will be included
 * in the generated JSON schema.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface SchemaDescription {
    /**
     * The description text for the field or type.
     */
    String value();
}

