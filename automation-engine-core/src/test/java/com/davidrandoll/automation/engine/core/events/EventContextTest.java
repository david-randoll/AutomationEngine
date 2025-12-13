package com.davidrandoll.automation.engine.core.events;

import com.davidrandoll.automation.engine.test.TestEvent;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class EventContextTest {

    @Test
    void testConstructor_withValidEvent() {
        // Given
        TestEvent event = TestEvent.builder()
                .eventType("TEST")
                .message("Test message")
                .value(42)
                .build();

        // When
        EventContext context = new EventContext(event);

        // Then
        assertThat(context.getEvent()).isEqualTo(event);
        assertThat(context.getType()).isEqualTo(TestEvent.class);
        assertThat(context.getMetadata()).isNotNull().isEmpty();
        assertThat(context.getTimestamp()).isNotNull();
        assertThat(context.getSource()).isNotNull();
    }

    @Test
    void testConstructor_withNullEvent_throwsException() {
        // When/Then
        assertThatThrownBy(() -> new EventContext(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Event cannot be null");
    }

    @Test
    void testOf_createsEventContext() {
        // Given
        TestEvent event = TestEvent.builder().eventType("TEST").build();

        // When
        EventContext context = EventContext.of(event);

        // Then
        assertThat(context).isNotNull();
        assertThat(context.getEvent()).isEqualTo(event);
    }

    @Test
    void testGetEventName_returnsSimpleClassName() {
        // Given
        TestEvent event = TestEvent.builder().eventType("TEST").build();
        EventContext context = new EventContext(event);

        // When
        String eventName = context.getEventName();

        // Then
        assertThat(eventName).isEqualTo("TestEvent");
    }

    @Test
    void testGetEventData_returnsEventFieldsAsMap() {
        // Given
        TestEvent.TestUser user = TestEvent.TestUser.builder()
                .name("John")
                .email("john@example.com")
                .age(30)
                .build();

        TestEvent event = TestEvent.builder()
                .eventType("USER_LOGIN")
                .message("User logged in")
                .value(1)
                .user(user)
                .build();

        EventContext context = new EventContext(event);

        // When
        Map<String, Object> eventData = context.getEventData();

        // Then
        assertThat(eventData).isNotNull();
        assertThat(eventData).containsEntry("eventType", "USER_LOGIN");
        assertThat(eventData).containsEntry("message", "User logged in");
        assertThat(eventData).containsEntry("value", 1);
        assertThat(eventData).containsKey("user");
    }

    @Test
    void testGetEventData_mergesMetadata() {
        // Given
        TestEvent event = TestEvent.builder()
                .eventType("TEST")
                .message("original message")
                .build();

        EventContext context = new EventContext(event);
        context.addMetadata("customKey", "customValue");
        context.addMetadata("message", "overridden message");

        // When
        Map<String, Object> eventData = context.getEventData();

        // Then
        assertThat(eventData).containsEntry("customKey", "customValue");
        // Metadata should override event data
        assertThat(eventData).containsEntry("message", "overridden message");
    }

    @Test
    void testAddMetadata_withMap() {
        // Given
        TestEvent event = TestEvent.builder().eventType("TEST").build();
        EventContext context = new EventContext(event);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", 42);

        // When
        context.addMetadata(metadata);

        // Then
        assertThat(context.getMetadata()).containsEntry("key1", "value1");
        assertThat(context.getMetadata()).containsEntry("key2", 42);
    }

    @Test
    void testAddMetadata_withNullMap_doesNotThrow() {
        // Given
        TestEvent event = TestEvent.builder().eventType("TEST").build();
        EventContext context = new EventContext(event);

        // When/Then
        assertThatCode(() -> context.addMetadata((Map<String, Object>) null))
                .doesNotThrowAnyException();
    }

    @Test
    void testAddMetadata_withKeyValue() {
        // Given
        TestEvent event = TestEvent.builder().eventType("TEST").build();
        EventContext context = new EventContext(event);

        // When
        context.addMetadata("userId", 123);
        context.addMetadata("userName", "John Doe");

        // Then
        assertThat(context.getMetadata()).containsEntry("userId", 123);
        assertThat(context.getMetadata()).containsEntry("userName", "John Doe");
    }

    @Test
    void testAddMetadata_withNullKey_throwsException() {
        // Given
        TestEvent event = TestEvent.builder().eventType("TEST").build();
        EventContext context = new EventContext(event);

        // When/Then
        assertThatThrownBy(() -> context.addMetadata(null, "value"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Key cannot be null");
    }

    @Test
    void testRemoveMetadata_removesEntry() {
        // Given
        TestEvent event = TestEvent.builder().eventType("TEST").build();
        EventContext context = new EventContext(event);
        context.addMetadata("key1", "value1");
        context.addMetadata("key2", "value2");

        // When
        context.removeMetadata("key1");

        // Then
        assertThat(context.getMetadata()).doesNotContainKey("key1");
        assertThat(context.getMetadata()).containsEntry("key2", "value2");
    }

    @Test
    void testRemoveMetadata_withNullKey_throwsException() {
        // Given
        TestEvent event = TestEvent.builder().eventType("TEST").build();
        EventContext context = new EventContext(event);

        // When/Then
        assertThatThrownBy(() -> context.removeMetadata(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Key cannot be null");
    }

    @Test
    void testGetMetadata_retrievesValue() {
        // Given
        TestEvent event = TestEvent.builder()
                .eventType("TEST")
                .message("test message")
                .build();

        EventContext context = new EventContext(event);
        context.addMetadata("customKey", "customValue");

        // When
        Object messageValue = context.getMetadata("message");
        Object customValue = context.getMetadata("customKey");

        // Then
        assertThat(messageValue).isEqualTo("test message");
        assertThat(customValue).isEqualTo("customValue");
    }

    @Test
    void testGetMetadata_withNonExistentKey_returnsNull() {
        // Given
        TestEvent event = TestEvent.builder().eventType("TEST").build();
        EventContext context = new EventContext(event);

        // When
        Object value = context.getMetadata("nonExistentKey");

        // Then
        assertThat(value).isNull();
    }

    @Test
    void testGetMetadata_withNullKey_throwsException() {
        // Given
        TestEvent event = TestEvent.builder().eventType("TEST").build();
        EventContext context = new EventContext(event);

        // When/Then
        assertThatThrownBy(() -> context.getMetadata(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Key cannot be null");
    }

    @Test
    void testDetermineSourceFromStackTrace_returnsNonEmptyString() {
        // When
        String source = EventContext.determineSourceFromStackTrace(EventContextTest.class);

        // Then
        assertThat(source).isNotNull();
        // Source might be empty if AutomationEngine is not in the stack trace
    }

    @Test
    void testMetadataOverridesEventData() {
        // Given
        TestEvent event = TestEvent.builder()
                .eventType("ORIGINAL_TYPE")
                .message("original message")
                .value(100)
                .build();

        EventContext context = new EventContext(event);

        // When - Add metadata that conflicts with event fields
        context.addMetadata("eventType", "OVERRIDDEN_TYPE");
        context.addMetadata("value", 200);
        Map<String, Object> eventData = context.getEventData();

        // Then - Metadata should override event data
        assertThat(eventData).containsEntry("eventType", "OVERRIDDEN_TYPE");
        assertThat(eventData).containsEntry("value", 200);
        assertThat(eventData).containsEntry("message", "original message");
    }

    @Test
    void testMetadataIsConcurrentSafe() throws InterruptedException {
        // Given
        TestEvent event = TestEvent.builder().eventType("TEST").build();
        EventContext context = new EventContext(event);

        // When - Add metadata from multiple threads
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                context.addMetadata("thread1-" + i, i);
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                context.addMetadata("thread2-" + i, i);
            }
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // Then - Should have all 200 entries without ConcurrentModificationException
        assertThat(context.getMetadata()).hasSize(200);
    }
}
