package com.davidrandoll.automation.engine.lua.results;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for LuaScriptResult.
 */
class LuaScriptResultTest extends AutomationEngineTest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class OrderEvent implements IEvent {
        private String orderId;
        private double amount;
        private String status;
    }

    @Test
    void testBasicResultGeneration() {
        var yaml = """
                alias: lua-result-basic
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Processing..."
                result:
                  result: luaScriptResult
                  script: |
                    return {
                      status = "success",
                      message = "Operation completed"
                    }
                """;

        Automation automation = factory.createAutomation("yaml", yaml);

        var context = EventContext.of(new OrderEvent("ORD-123", 99.99, "pending"));
        var result = engine.executeAutomationWithYaml(yaml, context);

        assertThat(result.isExecuted()).isTrue();
        JsonNode executionResult = (JsonNode) result.getExecutionResult();
        assertThat(executionResult.get("status").asText()).isEqualTo("success");
        assertThat(executionResult.get("message").asText()).isEqualTo("Operation completed");
    }

    @Test
    void testResultWithEventData() {
        var yaml = """
                alias: lua-result-event-data
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Processing order"
                result:
                  result: luaScriptResult
                  script: |
                    return {
                      orderId = event.orderId,
                      processedAmount = event.amount * 1.1,
                      originalStatus = event.status
                    }
                """;

        var context = EventContext.of(new OrderEvent("ORD-456", 100.0, "pending"));
        var result = engine.executeAutomationWithYaml(yaml, context);

        assertThat(result.isExecuted()).isTrue();
        JsonNode executionResult = (JsonNode) result.getExecutionResult();
        assertThat(executionResult.get("orderId").asText()).isEqualTo("ORD-456");
        assertThat(executionResult.get("processedAmount").asDouble()).isEqualTo(110.0);
        assertThat(executionResult.get("originalStatus").asText()).isEqualTo("pending");
    }

    @Test
    void testResultWithSimpleValue() {
        var yaml = """
                alias: lua-result-simple
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Processing..."
                result:
                  result: luaScriptResult
                  script: "return 42"
                """;

        var context = EventContext.of(new OrderEvent("ORD-789", 50.0, "completed"));
        var result = engine.executeAutomationWithYaml(yaml, context);

        assertThat(result.isExecuted()).isTrue();
        JsonNode executionResult = (JsonNode) result.getExecutionResult();
        assertThat(executionResult.asInt()).isEqualTo(42);
    }

    @Test
    void testResultWithStringValue() {
        var yaml = """
                alias: lua-result-string
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Processing..."
                result:
                  result: luaScriptResult
                  script: "return 'Order processed: ' .. event.orderId"
                """;

        var context = EventContext.of(new OrderEvent("ORD-999", 75.0, "completed"));
        var result = engine.executeAutomationWithYaml(yaml, context);

        assertThat(result.isExecuted()).isTrue();
        JsonNode executionResult = (JsonNode) result.getExecutionResult();
        assertThat(executionResult.asText()).isEqualTo("Order processed: ORD-999");
    }

    @Test
    void testResultWithArray() {
        var yaml = """
                alias: lua-result-array
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Processing..."
                result:
                  result: luaScriptResult
                  script: |
                    return {
                      items = {1, 2, 3, 4, 5},
                      tags = {"order", "processed", "complete"}
                    }
                """;

        var context = EventContext.of(new OrderEvent("ORD-111", 200.0, "completed"));
        var result = engine.executeAutomationWithYaml(yaml, context);

        assertThat(result.isExecuted()).isTrue();
        JsonNode executionResult = (JsonNode) result.getExecutionResult();
        assertThat(executionResult.get("items").isArray()).isTrue();
        assertThat(executionResult.get("items").size()).isEqualTo(5);
        assertThat(executionResult.get("tags").isArray()).isTrue();
    }

    @Test
    void testResultWithMetadata() {
        var yaml = """
                alias: lua-result-metadata
                variables:
                  - variable: basic
                    processedBy: "automation-engine"
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Processing..."
                result:
                  result: luaScriptResult
                  script: |
                    return {
                      orderId = event.orderId,
                      processor = metadata.processedBy
                    }
                """;

        var context = EventContext.of(new OrderEvent("ORD-222", 150.0, "pending"));
        var result = engine.executeAutomationWithYaml(yaml, context);

        assertThat(result.isExecuted()).isTrue();
        JsonNode executionResult = (JsonNode) result.getExecutionResult();
        assertThat(executionResult.get("orderId").asText()).isEqualTo("ORD-222");
        assertThat(executionResult.get("processor").asText()).isEqualTo("automation-engine");
    }

    @Test
    void testResultSkipsWhenScriptIsEmpty() {
        var yaml = """
                alias: lua-result-empty
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Processing..."
                result:
                  result: luaScriptResult
                  script: ""
                """;

        var context = EventContext.of(new OrderEvent("ORD-333", 100.0, "pending"));
        var result = engine.executeAutomationWithYaml(yaml, context);

        assertThat(result.isExecuted()).isTrue();
        JsonNode executionResult = (JsonNode) result.getExecutionResult();
        assertThat(executionResult.isNull()).isTrue();
    }

    @Test
    void testResultWithScriptError() {
        var yaml = """
                alias: lua-result-error
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Processing..."
                result:
                  result: luaScriptResult
                  script: "invalid lua {{"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new OrderEvent("ORD-444", 100.0, "pending"));

        assertThatThrownBy(() -> engine.publishEvent(context))
                .hasCauseInstanceOf(RuntimeException.class);
    }
}
