package com.davidrandoll.automation.engine.core.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReflectionUtilsTest {
    
    @Test
    void testBuildMapFromObject_withSimpleObject() {
        // Given
        @Data
        @AllArgsConstructor
        class TestObject {
            private String name;
            private int age;
        }
        
        TestObject obj = new TestObject("John", 30);
        
        // When
        Map<String, Object> result = ReflectionUtils.buildMapFromObject(obj);
        
        // Then
        assertThat(result).containsEntry("name", "John");
        assertThat(result).containsEntry("age", 30);
    }
    
    @Test
    void testBuildMapFromObject_withNullFields() {
        // Given
        @Data
        @AllArgsConstructor
        class TestObject {
            private String name;
            private Integer value;
        }
        
        TestObject obj = new TestObject(null, null);
        
        // When
        Map<String, Object> result = ReflectionUtils.buildMapFromObject(obj);
        
        // Then
        assertThat(result).containsEntry("name", null);
        assertThat(result).containsEntry("value", null);
    }
    
    @Test
    void testBuildMapFromObject_withNestedObject() {
        // Given
        @Data
        @AllArgsConstructor
        class Address {
            private String city;
        }
        
        @Data
        @AllArgsConstructor
        class Person {
            private String name;
            private Address address;
        }
        
        Person person = new Person("Jane", new Address("NYC"));
        
        // When
        Map<String, Object> result = ReflectionUtils.buildMapFromObject(person);
        
        // Then
        assertThat(result).containsKey("name");
        assertThat(result).containsKey("address");
        assertThat(result.get("name")).isEqualTo("Jane");
        assertThat(result.get("address")).isInstanceOf(Address.class);
    }
    
    @Test
    void testBuildMapFromObject_withEmptyObject() {
        // Given
        class EmptyObject {
        }
        
        EmptyObject obj = new EmptyObject();
        
        // When
        Map<String, Object> result = ReflectionUtils.buildMapFromObject(obj);
        
        // Then
        assertThat(result).isEmpty();
    }
}
