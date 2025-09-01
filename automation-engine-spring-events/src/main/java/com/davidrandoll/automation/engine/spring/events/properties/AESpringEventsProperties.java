package com.davidrandoll.automation.engine.spring.events.properties;

import lombok.Data;

import java.util.List;

@Data
public class AESpringEventsProperties {
    private boolean enabled = true;

    /**
     * The list of spring event types that are allowed to be republished to the AutomationEngine.
     * This is a security measure to prevent unwanted events from being processed by the AutomationEngine.
     * <p>
     * It does support regex patterns, so you can use wildcards to match multiple event types.
     */
    private List<String> allowedEventTypes = List.of(
            "com\\.davidrandoll\\.automation\\.engine\\..*"
    );
}