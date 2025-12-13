package com.davidrandoll.automation.engine.core.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilsTest {

    @Test
    void testToUserFriendlyName_withSimpleName() {
        // When
        String result = StringUtils.toUserFriendlyName(String.class);

        // Then
        assertThat(result).isEqualTo("String");
    }

    @Test
    void testToUserFriendlyName_withCamelCase() {
        // When
        String result = StringUtils.toUserFriendlyName(StringBuilder.class);

        // Then
        assertThat(result).isEqualTo("String Builder");
    }

    @Test
    void testToUserFriendlyName_withMultipleCamelCaseWords() {
        // When
        String result = StringUtils.toUserFriendlyName(IllegalArgumentException.class);

        // Then
        assertThat(result).isEqualTo("Illegal Argument Exception");
    }

    @Test
    void testToUserFriendlyName_startsWithUppercase() {
        // When
        String result = StringUtils.toUserFriendlyName(java.util.ArrayList.class);

        // Then
        assertThat(result).isEqualTo("Array List");
    }
}
