package com.davidrandoll.automation.engine;

import com.davidrandoll.automation.engine.actions.ObjectTypeTestAction;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestConfig.class)
class TemplatingIntegrationTest extends AutomationEngineTest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TestEvent implements IEvent {
        private String name;
        private String value;
    }

    // Simple test event implementation
    private static class SimpleEvent implements IEvent {
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
    void shouldExecuteWithAutomationLevelSpelTemplating() {
        String yaml = """
                alias: automation-level-spel-test
                options:
                  templatingType: "spel"
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    greeting: "Hello"
                    userName: "Charlie"
                actions:
                  - action: logger
                    message: "#{greeting} #{userName}!"
                """;

        TestEvent event = new TestEvent("Charlie", "789");
        var result = engine.executeAutomationWithYaml(yaml, event);

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("Hello Charlie!");
    }

    @Test
    void shouldPrioritizeBlockLevelOverAutomationLevelTemplating() {
        String yaml = """
                alias: priority-test
                options:
                  templatingType: "spel"
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    greeting: "Hello"
                    userName: "Dave"
                actions:
                  - action: logger
                    message: "Pebble: {{ greeting }} {{ userName }}!"
                    options:
                      templatingType: "pebble"
                  - action: logger
                    message: "Spel: #{greeting} #{userName}!"
                """;

        TestEvent event = new TestEvent("Dave", "000");
        var result = engine.executeAutomationWithYaml(yaml, event);

        assertThat(result.isExecuted()).isTrue();
        String logs = String.join("\n", logAppender.getLoggedMessages());
        assertThat(logs).contains("Pebble: Hello Dave!");
        assertThat(logs).contains("Spel: Hello Dave!");
    }

    @Test
    void shouldApplyAutomationLevelTemplatingToAllBlocks() {
        String yaml = """
                alias: all-blocks-spel-test
                options:
                  templatingType: "spel"
                triggers:
                  - trigger: alwaysTrue
                    description: "Trigger with #{event.name}"
                conditions:
                  - condition: alwaysTrue
                    description: "Condition with #{event.name}"
                variables:
                  - variable: basic
                    var1: "Value: #{event.value}"
                actions:
                  - action: logger
                    message: "Action: #{var1}"
                result:
                  summary: "Result: #{var1}"
                """;

        TestEvent event = new TestEvent("Eve", "111");
        var result = engine.executeAutomationWithYaml(yaml, event);

        assertThat(result.isExecuted()).isTrue();
        assertThat(String.join("\n", logAppender.getLoggedMessages())).contains("Action: Value: 111");
        assertThat(((JsonNode) result.get()).get("summary").asText()).isEqualTo("Result: Value: 111");
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
    }

    @Test
    void shouldReturnArrayWhenUsingSpel() {
        // Test that SpEL returns an actual array/list, not a string
        String yaml = """
                alias: spel-array-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    testArray: "#{new int[]{1, 2, 3, 4, 5}}"
                    options:
                      templatingType: "spel"
                actions:
                  - action: objectTypeTest
                    testValue: "#{testArray}"
                    options:
                      templatingType: "spel"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new SimpleEvent());
        assertThat(result.isExecuted()).isTrue();

        // Verify the action context to check what type was received
        var actionContext = ObjectTypeTestAction.lastContext;
        assertThat(actionContext).isNotNull();

        // Should receive a List (SpEL converts arrays to Lists)
        assertThat(actionContext.getReceivedType()).isEqualTo("List");
        assertThat(actionContext.getListSize()).isEqualTo(5);
        assertThat(actionContext.getTestValue()).isInstanceOf(List.class);
    }

    @Test
    void shouldReturnListWhenUsingSpel() {
        // Test that SpEL returns an actual List object, not a string
        String yaml = """
                alias: spel-list-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    testList: "#{{'apple', 'banana', 'cherry'}}"
                    options:
                      templatingType: "spel"
                actions:
                  - action: objectTypeTest
                    testValue: "#{testList}"
                    options:
                      templatingType: "spel"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new SimpleEvent());
        assertThat(result.isExecuted()).isTrue();

        var actionContext = ObjectTypeTestAction.lastContext;
        assertThat(actionContext).isNotNull();

        assertThat(actionContext.getReceivedType()).isEqualTo("List");
        assertThat(actionContext.getListSize()).isEqualTo(3);
        assertThat(actionContext.getTestValue()).isInstanceOf(List.class);

        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) actionContext.getTestValue();
        assertThat(list).containsExactly("apple", "banana", "cherry");
    }

    @Test
    void shouldReturnMapWhenUsingSpel() {
        // Test that SpEL returns an actual Map object, not a string
        String yaml = """
                alias: spel-map-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    testMap: "#{{name: 'John', age: 30, active: true}}"
                    options:
                      templatingType: "spel"
                actions:
                  - action: objectTypeTest
                    testValue: "#{testMap}"
                    options:
                      templatingType: "spel"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new SimpleEvent());
        assertThat(result.isExecuted()).isTrue();

        var actionContext = ObjectTypeTestAction.lastContext;
        assertThat(actionContext).isNotNull();

        assertThat(actionContext.getReceivedType()).isEqualTo("Map");
        assertThat(actionContext.getMapSize()).isEqualTo(3);
        assertThat(actionContext.getTestValue()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) actionContext.getTestValue();
        assertThat(map.get("name")).isEqualTo("John");
        assertThat(map.get("age")).isEqualTo(30);
        assertThat(map.get("active")).isEqualTo(true);
    }

    @Test
    void shouldReturnStringWhenUsingPebble() {
        // Test that Pebble still returns strings (for comparison)
        String yaml = """
                alias: pebble-string-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    testValue: "This is a string"
                actions:
                  - action: objectTypeTest
                    testValue: "{{ testValue }}"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new SimpleEvent());
        assertThat(result.isExecuted()).isTrue();

        var actionContext = ObjectTypeTestAction.lastContext;
        assertThat(actionContext).isNotNull();

        // Pebble should return a String
        assertThat(actionContext.getReceivedType()).isEqualTo("String");
        assertThat(actionContext.getTestValue()).isInstanceOf(String.class);
        assertThat(actionContext.getTestValue()).isEqualTo("This is a string");
    }

    @Test
    void shouldReturnIntegerWhenUsingSpel() {
        // Test that SpEL returns numeric types, not strings
        String yaml = """
                alias: spel-number-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    testNumber: "#{10 + 20}"
                    options:
                      templatingType: "spel"
                actions:
                  - action: objectTypeTest
                    testValue: "#{testNumber}"
                    options:
                      templatingType: "spel"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new SimpleEvent());
        assertThat(result.isExecuted()).isTrue();

        var actionContext = ObjectTypeTestAction.lastContext;
        assertThat(actionContext).isNotNull();

        assertThat(actionContext.getReceivedType()).isEqualTo("Integer");
        assertThat(actionContext.getTestValue()).isInstanceOf(Integer.class);
        assertThat(actionContext.getTestValue()).isEqualTo(30);
    }

    @Test
    void shouldReturnBooleanWhenUsingSpel() {
        // Test that SpEL returns boolean types, not strings
        String yaml = """
                alias: spel-boolean-test
                triggers:
                  - trigger: alwaysTrue
                variables:
                  - variable: basic
                    testBool: "#{5 > 3}"
                    options:
                      templatingType: "spel"
                actions:
                  - action: objectTypeTest
                    testValue: "#{testBool}"
                    options:
                      templatingType: "spel"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new SimpleEvent());
        assertThat(result.isExecuted()).isTrue();

        var actionContext = ObjectTypeTestAction.lastContext;
        assertThat(actionContext).isNotNull();

        assertThat(actionContext.getReceivedType()).isEqualTo("Boolean");
        assertThat(actionContext.getTestValue()).isInstanceOf(Boolean.class);
        assertThat(actionContext.getTestValue()).isEqualTo(true);
    }
}