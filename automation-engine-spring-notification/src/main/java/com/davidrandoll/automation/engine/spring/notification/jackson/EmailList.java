package com.davidrandoll.automation.engine.spring.notification.jackson;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for flexible email list deserialization.
 * <p>
 * Supports multiple input formats:
 * <ul>
 *     <li>Array: ["email1@test.com", "email2@test.com"]</li>
 *     <li>Comma-separated string: "email1@test.com,email2@test.com"</li>
 *     <li>Semicolon-separated string: "email1@test.com;email2@test.com"</li>
 *     <li>Single string: "email@test.com"</li>
 * </ul>
 * </p>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonDeserialize(using = EmailListDeserializer.class)
public @interface EmailList {
}
