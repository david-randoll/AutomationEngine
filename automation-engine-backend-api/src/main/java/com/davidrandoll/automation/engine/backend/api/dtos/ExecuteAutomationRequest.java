package com.davidrandoll.automation.engine.backend.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Request DTO for executing an automation in the playground.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteAutomationRequest {
    /**
     * The automation definition as a string (YAML or JSON).
     */
    private String automation;

    /**
     * The format of the automation definition.
     */
    @Builder.Default
    private AutomationFormat format = AutomationFormat.YAML;

    /**
     * Input data to be used as the event for the automation.
     */
    @Builder.Default
    private Map<String, Object> inputs = new HashMap<>();

    /**
     * Enum for specifying the automation format.
     */
    public enum AutomationFormat {
        YAML,
        JSON
    }
}
