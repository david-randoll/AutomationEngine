package com.davidrandoll.automation.engine.lua.triggers;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for LuaScriptTrigger.
 */
class LuaScriptTriggerTest extends AutomationEngineTest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestEvent implements IEvent {
        private String priority;
        private int count;
        private String status;
    }

    @Test
    void testTriggerActivatesWhenScriptReturnsTrue() {
        var yaml = """
                alias: lua-trigger-true
                triggers:
                  - trigger: luaScriptTrigger
                    script: "return true"
                actions:
                  - action: logger
                    message: "Trigger activated!"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TestEvent("HIGH", 5, "active"));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Trigger activated!"));
    }

    @Test
    void testTriggerDoesNotActivateWhenScriptReturnsFalse() {
        var yaml = """
                alias: lua-trigger-false
                triggers:
                  - trigger: luaScriptTrigger
                    script: "return false"
                actions:
                  - action: logger
                    message: "This should not appear"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TestEvent("HIGH", 5, "active"));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("This should not appear"));
    }

    @Test
    void testTriggerWithEventDataCondition() {
        var yaml = """
                alias: lua-trigger-event-condition
                triggers:
                  - trigger: luaScriptTrigger
                    script: "return priority == 'HIGH' and count > 3"
                actions:
                  - action: logger
                    message: "High priority event with count > 3"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Should trigger
        var context1 = EventContext.of(new TestEvent("HIGH", 5, "active"));
        engine.publishEvent(context1);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("High priority event with count > 3"));
    }

    @Test
    void testTriggerWithEventDataConditionNotMet() {
        var yaml = """
                alias: lua-trigger-event-condition-not-met
                triggers:
                  - trigger: luaScriptTrigger
                    script: "return priority == 'HIGH' and count > 10"
                actions:
                  - action: logger
                    message: "This should not appear"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Should not trigger - count is only 5
        var context = EventContext.of(new TestEvent("HIGH", 5, "active"));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("This should not appear"));
    }

    @Test
    void testTriggerWithComplexLogic() {
        var yaml = """
                alias: lua-trigger-complex
                triggers:
                  - trigger: luaScriptTrigger
                    script: |
                      local isHighPriority = priority == 'HIGH'
                      local hasMinCount = count >= 5
                      local isActive = status == 'active'
                      return isHighPriority and hasMinCount and isActive
                actions:
                  - action: logger
                    message: "All conditions met!"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TestEvent("HIGH", 10, "active"));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("All conditions met!"));
    }

    @Test
    void testTriggerSkipsWhenScriptIsEmpty() {
        var yaml = """
                alias: lua-trigger-empty
                triggers:
                  - trigger: luaScriptTrigger
                    script: ""
                actions:
                  - action: logger
                    message: "This should not appear"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TestEvent("HIGH", 5, "active"));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("This should not appear"));
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("LuaScriptTrigger skipped"));
    }

    @Test
    void testTriggerWithScriptError() {
        var yaml = """
                alias: lua-trigger-error
                triggers:
                  - trigger: luaScriptTrigger
                    script: "invalid lua {{"
                actions:
                  - action: logger
                    message: "This should not appear"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TestEvent("HIGH", 5, "active"));

        assertThatThrownBy(() -> engine.publishEvent(context))
                .hasCauseInstanceOf(RuntimeException.class);
    }
}
