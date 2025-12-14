package com.davidrandoll.automation.engine.test;

import com.davidrandoll.automation.engine.core.events.IEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple test event for unit testing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestEvent implements IEvent {
    private String eventType;
    private String message;
    private Integer value;
    private TestUser user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestUser {
        private String name;
        private String email;
        private Integer age;
    }
}
