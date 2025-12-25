package com.davidrandoll.automation.engine.lua.actions;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.modules.events.time_based.TimeBasedEvent;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for LuaScriptAction.
 */
class LuaScriptActionTest extends AutomationEngineTest {

    @Test
    void testBasicLuaScriptExecution() {
        var yaml = """
                alias: lua-action-basic
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: luaScript
                    script: |
                      log.info("Lua script executed!")
                      return true
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("[Lua] Lua script executed!"));
    }

    @Test
    void testLuaScriptAccessesEventData() {
        var yaml = """
                alias: lua-action-event-access
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: luaScript
                    script: |
                      log.info("Event time: " .. tostring(time))
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(14, 30)));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("[Lua] Event time:"));
    }

    @Test
    void testLuaScriptAddsToMetadata() {
        var yaml = """
                alias: lua-action-metadata
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: luaScript
                    storeToVariable: "lua"
                    script: |
                      return {
                        computedValue = 42,
                        greeting = "Hello from Lua"
                      }
                  - action: logger
                    message: "Computed: {{ lua.computedValue }}, Greeting: {{ lua.greeting }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Computed: 42, Greeting: Hello from Lua"));
    }

    @Test
    void testLuaScriptWithComplexLogic() {
        var yaml = """
                alias: lua-action-complex
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: luaScript
                    storeToVariable: "lua"
                    script: |
                      local numbers = {1, 2, 3, 4, 5}
                      local sum = 0
                      for i, v in ipairs(numbers) do
                        sum = sum + v
                      end
                      return {
                        sum = sum,
                        average = sum / #numbers
                      }
                  - action: logger
                    message: "Sum: {{ lua.sum }}, Average: {{ lua.average }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Sum: 15, Average: 3"));
    }

    @Test
    void testLuaScriptSkipsWhenEmpty() {
        var yaml = """
                alias: lua-action-empty-script
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: luaScript
                    script: ""
                  - action: logger
                    message: "After empty script"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("LuaScriptAction skipped: script is null or blank"));
    }

    @Test
    void testLuaScriptError() {
        var yaml = """
                alias: lua-action-error
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: luaScript
                    script: "invalid lua {{"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));

        assertThatThrownBy(() -> engine.publishEvent(context))
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void testLuaScriptJsonEncodeDecode() {
        var yaml = """
                alias: lua-action-json
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: luaScript
                    storeToVariable: "lua"
                    script: |
                      local data = {name = "test", value = 123}
                      local encoded = json.encode(data)
                      local decoded = json.decode(encoded)
                      return {
                        originalName = data.name,
                        encodedJson = encoded,
                        decodedName = decoded.name
                      }
                  - action: logger
                    message: "Decoded name: {{ lua.decodedName }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Decoded name: test"));
    }
}
