package com.davidrandoll.automation.engine.creator.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventFactoryTest {
    
    private EventFactory factory;
    private ObjectMapper mapper;
    
    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        factory = new EventFactory(mapper);
    }
    
    @Test
    void testCreateEvent_fromJsonNode() {
        // Given
        JsonNode jsonNode = JsonNodeFactory.instance.objectNode()
                .put("type", "test")
                .put("value", 123);
        
        // When
        JsonEvent result = factory.createEvent(jsonNode);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEvent().get("type").asText()).isEqualTo("test");
        assertThat(result.getEvent().get("value").asInt()).isEqualTo(123);
    }
    
    @Test
    void testCreateEvent_fromObject() {
        // Given
        record TestEvent(String name, int value) {}
        TestEvent event = new TestEvent("test-event", 42);
        
        // When
        JsonEvent result = factory.createEvent(event);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEvent().get("name").asText()).isEqualTo("test-event");
        assertThat(result.getEvent().get("value").asInt()).isEqualTo(42);
        assertThat(result.getEventType()).contains("TestEvent");
    }
    
    @Test
    void testCreateEvent_fromObjectWithNullFields() {
        // Given
        record TestEvent(String name, Integer value) {}
        TestEvent event = new TestEvent(null, null);
        
        // When
        JsonEvent result = factory.createEvent(event);
        
        // Then
        assertThat(result).isNotNull();
    }
    
    @Test
    void testCreateEvent_fromNullObject_throwsException() {
        // When/Then
        assertThatThrownBy(() -> factory.createEvent((Object) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot create event from null object");
    }
    
    @Test
    void testCreateEvent_fromComplexObject() {
        // Given
        record Address(String city, String state) {}
        record Person(String name, int age, Address address) {}
        Person person = new Person("John", 30, new Address("NYC", "NY"));
        
        // When
        JsonEvent result = factory.createEvent(person);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEvent().get("name").asText()).isEqualTo("John");
        assertThat(result.getEvent().get("age").asInt()).isEqualTo(30);
        assertThat(result.getEventType()).contains("Person");
    }
}
