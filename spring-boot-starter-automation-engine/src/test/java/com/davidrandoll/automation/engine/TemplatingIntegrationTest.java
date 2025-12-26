package com.davidrandoll.automation.engine;

import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TemplatingIntegrationTest extends AutomationEngineTest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TestEvent implements IEvent {
        private String name;
        private String value;
    }

    @Test
    void shouldExecuteWithDefaultPebbleTemplating() {
        String yaml = """
                alias: pebble-test
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Hello {{ event.name }}!"
                """;

        TestEvent event = new TestEvent("Alice", "123");
        var result = engine.executeAutomationWithYaml(yaml, event);

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("Hello Alice!");
    }

    @Test
    void shouldExecuteWithExplicitSpelTemplating() {
        String yaml = """
                alias: spel-test
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Hello #{event.name}!"
                    options:
                      templatingType: "spel"
                """;

        TestEvent event = new TestEvent("Bob", "456");
        var result = engine.executeAutomationWithYaml(yaml, event);

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("Hello Bob!");
    }

    @Test
    void shouldExecuteWithMixedTemplating() {
        String yaml = """
                alias: mixed-test
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Pebble: {{ event.name }}"
                  - action: logger
                    message: "SpEL: #{event.name}"
                    options:
                      templatingType: "spel"
                """;

        TestEvent event = new TestEvent("Charlie", "789");
        var result = engine.executeAutomationWithYaml(yaml, event);

        assertThat(result.isExecuted()).isTrue();
        String output = String.join("\n", logAppender.getLoggedMessages());
        assertThat(output).contains("Pebble: Charlie");
        assertThat(output).contains("SpEL: Charlie");
    }

    @Test
    void shouldExecuteWithSpelInVariables() {
        String yaml = """
                alias: spel-var-test
                variables:
                  - variable: basic
                    alias: greetingVar
                    greeting: "Hi #{event.name}"
                    options:
                      templatingType: "spel"
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "{{ greeting }} from Pebble"
                """;

        TestEvent event = new TestEvent("David", "000");
        var result = engine.executeAutomationWithYaml(yaml, event);

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("Hi David from Pebble");
    }

    @Test
    void shouldExecuteWithSpelInConditions() {
        String yaml = """
                alias: spel-condition-test
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: alwaysTrue
                    options:
                      templatingType: "spel"
                actions:
                  - action: logger
                    message: "Condition met with SpEL options"
                """;

        TestEvent event = new TestEvent("Eve", "111");
        var result = engine.executeAutomationWithYaml(yaml, event);

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("Condition met with SpEL options");
    }

    @Test
    void shouldHandleNestedSpelTemplating() {
        String yaml = """
                alias: nested-spel-test
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message:
                      text: "Nested #{event.name}"
                      details:
                        val: "#{event.value}"
                    options:
                      templatingType: "spel"
                """;

        TestEvent event = new TestEvent("Frank", "999");
        var result = engine.executeAutomationWithYaml(yaml, event);

        assertThat(result.isExecuted()).isTrue();
        // LoggerAction usually logs the message as string if it's a map
        String output = String.join("\n", logAppender.getLoggedMessages());
        assertThat(output).contains("Nested Frank");
        assertThat(output).contains("999");
    }
}
