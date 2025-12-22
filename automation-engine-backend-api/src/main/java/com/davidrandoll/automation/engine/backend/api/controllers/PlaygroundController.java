package com.davidrandoll.automation.engine.backend.api.controllers;

import com.davidrandoll.automation.engine.AutomationEngine;
import com.davidrandoll.automation.engine.backend.api.dtos.ExecuteAutomationRequest;
import com.davidrandoll.automation.engine.backend.api.dtos.ExecuteAutomationResponse;
import com.davidrandoll.automation.engine.backend.api.dtos.PlaygroundEvent;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import com.davidrandoll.automation.engine.tracing.ExecutionTrace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for the automation playground.
 * Provides endpoints for executing automations with tracing enabled.
 */
@Slf4j
@RestController
@RequestMapping("${automation-engine.backend-api.path:/automation-engine}/playground")
@RequiredArgsConstructor
public class PlaygroundController {
    private final AutomationEngine automationEngine;

    /**
     * Execute an automation with the provided definition and inputs.
     * Tracing is enabled by default in the automation options if not specified.
     *
     * @param request the execution request containing automation definition and
     *                inputs
     * @return the execution response with result and trace
     */
    @PostMapping("/execute")
    public ResponseEntity<ExecuteAutomationResponse> executeAutomation(
            @RequestBody ExecuteAutomationRequest request) {
        try {
            // Create event from inputs
            PlaygroundEvent event = PlaygroundEvent.from(request.getInputs());
            EventContext eventContext = new EventContext(event);

            // Ensure tracing is enabled - prepend options if not present
            String automation = ensureTracingEnabled(request.getAutomation(), request.getFormat());

            // Execute the automation
            AutomationResult result;
            if (request.getFormat() == ExecuteAutomationRequest.AutomationFormat.JSON) {
                result = automationEngine.executeAutomationWithJson(automation, eventContext);
            } else {
                result = automationEngine.executeAutomationWithYaml(automation, eventContext);
            }

            // Extract trace from result
            ExecutionTrace trace = null;
            if (result.getAdditionalFields() != null
                    && result.getAdditionalFields().containsKey(ExecutionTrace.TRACE_KEY)) {
                trace = (ExecutionTrace) result.getAdditionalFields().get(ExecutionTrace.TRACE_KEY);
            }

            return ResponseEntity.ok(ExecuteAutomationResponse.builder()
                    .executed(result.isExecuted())
                    .result(result.getResult())
                    .trace(trace)
                    .build());
        } catch (Exception e) {
            log.error("Failed to execute automation", e);
            return ResponseEntity.ok(ExecuteAutomationResponse.builder()
                    .executed(false)
                    .error(e.getMessage())
                    .build());
        }
    }

    /**
     * Ensures that tracing is enabled in the automation definition.
     * If options.tracing is not specified, it adds it.
     */
    private String ensureTracingEnabled(String automation, ExecuteAutomationRequest.AutomationFormat format) {
        if (automation == null) {
            return automation;
        }

        // Check if tracing option is already set
        if (automation.contains("tracing:") || automation.contains("\"tracing\"")) {
            return automation;
        }

        // Add tracing option based on format
        if (format == ExecuteAutomationRequest.AutomationFormat.JSON) {
            // For JSON, try to add to options object
            if (automation.contains("\"options\"")) {
                // Options exists, add tracing to it
                return automation.replaceFirst(
                        "\"options\"\\s*:\\s*\\{",
                        "\"options\": { \"tracing\": true, ");
            } else {
                // Add options with tracing after the first {
                return automation.replaceFirst(
                        "\\{",
                        "{ \"options\": { \"tracing\": true }, ");
            }
        } else {
            // For YAML, add options at the beginning
            if (automation.contains("options:")) {
                // Options exists, add tracing under it
                return automation.replaceFirst(
                        "options:",
                        "options:\n  tracing: true");
            } else {
                // Add options block at the beginning
                return "options:\n  tracing: true\n" + automation;
            }
        }
    }
}
