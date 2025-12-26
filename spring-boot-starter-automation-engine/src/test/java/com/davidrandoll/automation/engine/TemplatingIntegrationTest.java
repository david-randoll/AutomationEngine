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
                variables:
                  - variable: basic
                    greeting: "Hello"
                    userName: "Alice"
                actions:
                  - action: logger
                    message: "{{ greeting }} {{ userName }}!"
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
                variables:
                  - variable: basic
                    greeting: "Hello"
                    userName: "Bob"
                    options:
                      templatingType: "spel"
                actions:
                  - action: logger
                    message: "#{greeting} #{userName}!"
                    options:
                      templatingType: "spel"
                """;

        TestEvent event = new TestEvent("Bob", "456");
        var result = engine.executeAutomationWithYaml(yaml, event);

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("Hello Bob!");
    }

    @Test
    void shouldUseDefaultPebbleWhenTemplatingTypeNotSpecified() {
        String yaml = """
                alias: default-engine-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    defaultName: "DefaultTest"
                actions:
                  - action: logger
                    message: "Default: {{ defaultName }}"
                """;

        TestEvent event = new TestEvent("Test", "999");
        var result = engine.executeAutomationWithYaml(yaml, event);

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("Default: DefaultTest");
    }

    @Test
    void shouldFallbackToPebbleOnInvalidTemplatingType() {
        String yaml = """
                alias: fallback-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    testName: "Fallback"
                actions:
                  - action: logger
                    message: "Fallback: {{ testName }}"
                    options:
                      templatingType: "invalid-engine-type"
                """;

        TestEvent event = new TestEvent("Fallback", "888");
        var result = engine.executeAutomationWithYaml(yaml, event);

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("Fallback: Fallback");
    }

    @Test
    void shouldHandleCaseInsensitiveTemplatingType() {
        String yaml = """
                alias: case-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    greeting: "Hello"
                    userName: "CaseTest"
                    options:
                      templatingType: "SPEL"
                actions:
                  - action: logger
                    message: "#{greeting} #{userName}!"
                    options:
                      templatingType: "SPEL"
                """;

        TestEvent event = new TestEvent("CaseTest", "500");
        var result = engine.executeAutomationWithYaml(yaml, event);

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("Hello CaseTest!");
    }

    // ============ SpEL Math Operations ============
    @Test
    void shouldPerformSpelMathOperations() {
        String yaml = """
                alias: spel-math-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    num1: 10
                    num2: 5
                    options:
                      templatingType: "spel"
                actions:
                  - action: logger
                    message: "#{num1 + num2}, #{num1 - num2}, #{num1 * num2}, #{num1 / num2}"
                    options:
                      templatingType: "spel"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new TestEvent("math", "test"));

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("15, 5, 50, 2");
    }

    @Test
    void shouldPerformSpelStringConcatenation() {
        String yaml = """
                alias: spel-concat-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    firstName: "John"
                    lastName: "Doe"
                    options:
                      templatingType: "spel"
                actions:
                  - action: logger
                    message: "#{firstName + ' ' + lastName}"
                    options:
                      templatingType: "spel"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new TestEvent("concat", "test"));

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("John Doe");
    }

    @Test
    void shouldHandleSpelTernaryOperator() {
        String yaml = """
                alias: spel-ternary-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    isActive: true
                    options:
                      templatingType: "spel"
                actions:
                  - action: logger
                    message: "#{isActive ? 'Active' : 'Inactive'}"
                    options:
                      templatingType: "spel"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new TestEvent("ternary", "test"));

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("Active");
    }

    @Test
    void shouldHandleSpelNullSafeNavigation() {
        String yaml = """
                alias: spel-nullsafe-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    nullValue: null
                    nonNullValue: "exists"
                    options:
                      templatingType: "spel"
                actions:
                  - action: logger
                    message: "#{nullValue?.toString()}, #{nonNullValue?.toString()}"
                    options:
                      templatingType: "spel"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new TestEvent("nullsafe", "test"));

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains(", exists");
    }

    @Test
    void shouldHandleSpelComparisonOperators() {
        String yaml = """
                alias: spel-comparison-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    x: 10
                    y: 5
                actions:
                  - action: logger
                    message: "Greater: #{x > y}, Less: #{x < y}"
                    options:
                      templatingType: "spel"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new TestEvent("comparison", "test"));

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("Greater: true, Less: false");
    }

    // ============ Pebble Filter Tests ============
    @Test
    void shouldApplyPebbleUpperFilter() {
        String yaml = """
                alias: pebble-upper-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    text: "hello world"
                actions:
                  - action: logger
                    message: "{{ text | upper }}"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new TestEvent("upper", "test"));

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("HELLO WORLD");
    }

    @Test
    void shouldApplyPebbleLowerFilter() {
        String yaml = """
                alias: pebble-lower-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    text: "HELLO WORLD"
                actions:
                  - action: logger
                    message: "{{ text | lower }}"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new TestEvent("lower", "test"));

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("hello world");
    }

    @Test
    void shouldApplyPebbleCapitalizeFilter() {
        String yaml = """
                alias: pebble-capitalize-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    text: "hello world"
                actions:
                  - action: logger
                    message: "{{ text | capitalize }}"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new TestEvent("capitalize", "test"));

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("Hello world");
    }

    @Test
    void shouldApplyPebbleLengthFilter() {
        String yaml = """
                alias: pebble-length-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    text: "Hello"
                actions:
                  - action: logger
                    message: "{{ text | length }}"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new TestEvent("length", "test"));

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("5");
    }

    @Test
    void shouldApplyPebbleDefaultFilter() {
        String yaml = """
                alias: pebble-default-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    nullValue: null
                actions:
                  - action: logger
                    message: "{{ nullValue | default('fallback') }}"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new TestEvent("default", "test"));

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("fallback");
    }

    @Test
    void shouldChainPebbleFilters() {
        String yaml = """
                alias: pebble-chain-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    text: "hello world"
                actions:
                  - action: logger
                    message: "{{ text | upper | capitalize }}"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new TestEvent("chain", "test"));

        assertThat(result.isExecuted()).isTrue();
        // capitalize after upper results in the string
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("HELLO WORLD");
    }

    // ============ Mixed Engine Tests ============
    @Test
    void shouldUseDifferentEnginesForVariablesAndActions() {
        String yaml = """
                alias: mixed-engine-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    varX: 10
                    varY: 5
                    options:
                      templatingType: "spel"
                actions:
                  - action: logger
                    message: "Result: {{ varX + varY }}"
                    options:
                      templatingType: "pebble"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new TestEvent("mixed", "test"));

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("Result: 15");
    }

    @Test
    void shouldIsolateEngineOptionsPerComponent() {
        String yaml = """
                alias: isolation-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    spelVar: "SpEL"
                    pebbleVar: "Pebble"
                actions:
                  - action: logger
                    message: "#{spelVar + ' and ' + pebbleVar}"
                    options:
                      templatingType: "spel"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new TestEvent("isolation", "test"));

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("SpEL and Pebble");
    }

    // ============ Error Handling Tests ============
    @Test
    void shouldHandleUndefinedVariableGracefully() {
        String yaml = """
                alias: undefined-var-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    knownVar: "known"
                actions:
                  - action: logger
                    message: "{{ knownVar }} {{ unknownVar }}"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new TestEvent("undefined", "test"));

        // Should execute without throwing exception
        assertThat(result.isExecuted()).isTrue();
    }

    @Test
    void shouldHandleEmptyStringInSpel() {
        String yaml = """
                alias: empty-string-spel-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    emptyStr: ""
                    options:
                      templatingType: "spel"
                actions:
                  - action: logger
                    message: "Empty: #{emptyStr}"
                    options:
                      templatingType: "spel"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new TestEvent("empty", "test"));

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("Empty: ");
    }

    @Test
    void shouldHandleSpecialCharactersInPebble() {
        String yaml = """
                alias: special-chars-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    special: "Hello & <World>"
                actions:
                  - action: logger
                    message: "{{ special }}"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new TestEvent("special", "test"));

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("Hello & <World>");
    }

    // ============ Complex Expression Tests ============
    @Test
    void shouldHandleComplexSpelExpression() {
        String yaml = """
                alias: complex-spel-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    price: 100
                    discount: 0.2
                    taxRate: 0.1
                    options:
                      templatingType: "spel"
                actions:
                  - action: logger
                    message: "#{(price * (1 - discount)) * (1 + taxRate)}"
                    options:
                      templatingType: "spel"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new TestEvent("complex", "test"));

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("88.0");
    }

    @Test
    void shouldHandleSpelWithBooleanLogic() {
        String yaml = """
                alias: boolean-logic-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    isAdmin: true
                    isActive: true
                    options:
                      templatingType: "spel"
                actions:
                  - action: logger
                    message: "#{isAdmin and isActive ? 'Full Access' : 'Limited'}"
                    options:
                      templatingType: "spel"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new TestEvent("boolean", "test"));

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("Full Access");
    }

    @Test
    void shouldHandlePebbleConditionalExpression() {
        String yaml = """
                alias: pebble-conditional-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    age: 25
                actions:
                  - action: logger
                    message: "{% if age >= 18 %}Adult{% else %}Minor{% endif %}"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new TestEvent("conditional", "test"));

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("Adult");
    }

    @Test
    void shouldHandleNestedTemplating() {
        String yaml = """
                alias: nested-template-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    firstName: "John"
                    lastName: "Doe"
                actions:
                  - action: logger
                    message: "{{ 'Mr. ' + firstName + ' ' + lastName }}"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new TestEvent("nested", "test"));

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("Mr. John Doe");
    }

    @Test
    void shouldHandleNumericPrecisionInSpel() {
        String yaml = """
                alias: precision-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    value1: 0.1
                    value2: 0.2
                    options:
                      templatingType: "spel"
                actions:
                  - action: logger
                    message: "#{value1 + value2}"
                    options:
                      templatingType: "spel"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new TestEvent("precision", "test"));

        assertThat(result.isExecuted()).isTrue();
        // May be 0.30000000000000004 due to floating point arithmetic
        assertThat(String.join("\n", logAppender.getLoggedMessages())).containsPattern("0\\.3\\d*");
    }}