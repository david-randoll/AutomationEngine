package com.davidrandoll.automation.engine;

import com.davidrandoll.automation.engine.actions.ObjectTypeTestAction;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.creator.AutomationFactory;
import com.davidrandoll.automation.engine.lua.LuaScriptEngine;
import com.davidrandoll.automation.engine.orchestrator.IAEOrchestrator;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestConfig.class)
class AEStarterTest extends AutomationEngineTest {

    @Autowired
    private ApplicationContext context;

    // Simple test event implementation
    private static class SimpleEvent implements IEvent {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class LuaTestEvent implements IEvent {
        private String name;
        private int value;
    }

    @Test
    void shouldLoadAllRequiredBeans() {
        // Verify critical beans are loaded by the starter
        assertThat(engine).isNotNull();
        assertThat(context.getBean(IAEOrchestrator.class)).isNotNull();
        assertThat(context.getBean(AutomationFactory.class)).isNotNull();
    }

    @Test
    void shouldLoadLuaScriptEngine() {
        // Verify Lua beans are loaded by the starter
        assertThat(context.getBean(LuaScriptEngine.class)).isNotNull();
    }

    @Test
    void shouldExecuteBasicAutomationFromStarter() {
        // Test actual functionality works when loaded through starter
        String yaml = """
                alias: starter-test
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Starter working"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new SimpleEvent());
        assertThat(result.isExecuted()).isTrue();
        assertThat(result.getAutomation().getAlias()).isEqualTo("starter-test");
    }

    @Test
    void shouldRegisterAndExecuteAutomation() {
        // Test registration works through starter
        String yaml = """
                alias: registration-test
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Registration test"
                """;

        engine.registerWithYaml(yaml);
        engine.publishEvent(new SimpleEvent());

        // Verify the automation was registered (publishEvent doesn't throw an exception)
        assertThat(engine).isNotNull();
    }

    @Test
    void shouldExecuteLuaScriptAction() {
        // Test Lua script action works through starter
        String yaml = """
                alias: lua-starter-action-test
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: luaScript
                    script: |
                      return {
                        greeting = "Hello from Lua!",
                        computed = 10 + 20
                      }
                result:
                  result: basic
                  greeting: "{{ greeting }}"
                  computed: "{{ computed }}"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new SimpleEvent());
        assertThat(result.isExecuted()).isTrue();
    }

    @Test
    void shouldExecuteLuaScriptTrigger() {
        // Test Lua script trigger works through starter
        String yaml = """
                alias: lua-starter-trigger-test
                triggers:
                  - trigger: luaScriptTrigger
                    script: "return value > 5"
                actions:
                  - action: logger
                    message: "Lua trigger activated"
                """;

        // Should trigger - value is 10
        var result = engine.executeAutomationWithYaml(yaml, new LuaTestEvent("test", 10));
        assertThat(result.isExecuted()).isTrue();

        // Should not trigger - value is 3
        var result2 = engine.executeAutomationWithYaml(yaml, new LuaTestEvent("test", 3));
        assertThat(result2.isExecuted()).isFalse();
    }

    @Test
    void shouldExecuteLuaScriptCondition() {
        // Test Lua script condition works through starter
        String yaml = """
                alias: lua-starter-condition-test
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: luaScriptCondition
                    script: "return name == 'admin'"
                actions:
                  - action: logger
                    message: "Admin access"
                """;

        // Should pass - name is 'admin'
        var result = engine.executeAutomationWithYaml(yaml, new LuaTestEvent("admin", 5));
        assertThat(result.isExecuted()).isTrue();

        // Should fail - name is 'user'
        var result2 = engine.executeAutomationWithYaml(yaml, new LuaTestEvent("user", 5));
        assertThat(result2.isExecuted()).isFalse();
    }

    @Test
    void shouldExecuteLuaScriptVariable() {
        // Test Lua script variable works through starter
        String yaml = """
                alias: lua-starter-variable-test
                variables:
                  - variable: luaScriptVariable
                    script: |
                      return {
                        upperName = string.upper(name),
                        doubleValue = value * 2
                      }
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Name: {{ upperName }}, Double: {{ doubleValue }}"
                """;

        var result = engine.executeAutomationWithYaml(yaml, new LuaTestEvent("test", 7));
        assertThat(result.isExecuted()).isTrue();
    }

    @Test
    void shouldExecuteLuaScriptResult() {
        // Test Lua script result works through starter
        String yaml = """
                alias: lua-starter-result-test
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Processing"
                result:
                  result: luaScriptResult
                  script: |
                    return {
                      status = "success",
                      eventName = name,
                      processedValue = value * 3
                    }
                """;

        var result = engine.executeAutomationWithYaml(yaml, new LuaTestEvent("myEvent", 5));
        assertThat(result.isExecuted()).isTrue();

        JsonNode executionResult = (JsonNode) result.get();
        assertThat(executionResult.get("status").asText()).isEqualTo("success");
        assertThat(executionResult.get("eventName").asText()).isEqualTo("myEvent");
        assertThat(executionResult.get("processedValue").asInt()).isEqualTo(15);
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