package com.davidrandoll.automation.engine.spring.tracing;

import com.davidrandoll.automation.engine.AutomationEngine;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for tracing configuration properties.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "automation-engine.tracing.enabled=false"
})
class TracingConfigurationTest {
    
    @Autowired
    private AutomationEngine engine;
    
    @Test
    void shouldNotTraceWhenDisabled() {
        // Given: Tracing is disabled
        String yaml = """
            alias: no-trace
            triggers:
              - trigger: always
            actions:
              - action: logMessage
                message: "Test"
            """;
        
        IEvent event = new TestEvent("test");
        
        // When: Execute automation
        AutomationResult result = engine.executeAutomationWithYaml(yaml, event);
        
        // Then: No trace data in result
        assertThat(result.isExecuted()).isTrue();
        assertThat(result.getAdditionalFields()).isEmpty();
        
        Map<String, Object> trace = AutomationTraceExtractor.extractTrace(result);
        assertThat(trace).isEmpty();
    }
    
    @Data
    static class TestEvent implements IEvent {
        private final String name;
    }
}
