package com.davidrandoll.automation.engine;

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

import static org.assertj.core.api.Assertions.assertThat;

class LuaTest extends AutomationEngineTest {

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
}