package com.davidrandoll.automation.engine.lua.conditions;

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
 * Integration tests for LuaScriptCondition.
 */
class LuaScriptConditionTest extends AutomationEngineTest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestEvent implements IEvent {
        private String userRole;
        private int accessLevel;
        private boolean isVerified;
    }

    @Test
    void testConditionPassesWhenScriptReturnsTrue() {
        var yaml = """
                alias: lua-condition-true
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: luaScriptCondition
                    script: "return true"
                actions:
                  - action: logger
                    message: "Condition passed!"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TestEvent("admin", 5, true));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Condition passed!"));
    }

    @Test
    void testConditionFailsWhenScriptReturnsFalse() {
        var yaml = """
                alias: lua-condition-false
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: luaScriptCondition
                    script: "return false"
                actions:
                  - action: logger
                    message: "This should not appear"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TestEvent("user", 1, false));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("This should not appear"));
    }

    @Test
    void testConditionWithEventDataCheck() {
        var yaml = """
                alias: lua-condition-event-check
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: luaScriptCondition
                    script: "return userRole == 'admin' and accessLevel >= 5"
                actions:
                  - action: logger
                    message: "Admin access granted!"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Should pass - admin with level 5
        var context1 = EventContext.of(new TestEvent("admin", 5, true));
        engine.publishEvent(context1);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Admin access granted!"));
    }

    @Test
    void testConditionWithEventDataCheckFails() {
        var yaml = """
                alias: lua-condition-event-check-fails
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: luaScriptCondition
                    script: "return userRole == 'admin' and accessLevel >= 10"
                actions:
                  - action: logger
                    message: "This should not appear"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Should fail - level is only 5
        var context = EventContext.of(new TestEvent("admin", 5, true));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("This should not appear"));
    }

    @Test
    void testConditionWithComplexLogic() {
        var yaml = """
                alias: lua-condition-complex
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: luaScriptCondition
                    script: |
                      -- Check if user is admin or verified user with high access
                      local isAdmin = userRole == 'admin'
                      local isVerifiedPowerUser = isVerified and accessLevel >= 3
                      return isAdmin or isVerifiedPowerUser
                actions:
                  - action: logger
                    message: "Access granted!"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Should pass - verified user with access level 3
        var context = EventContext.of(new TestEvent("user", 3, true));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Access granted!"));
    }

    @Test
    void testMultipleLuaConditions() {
        var yaml = """
                alias: lua-multiple-conditions
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: luaScriptCondition
                    script: "return userRole == 'admin'"
                  - condition: luaScriptCondition
                    script: "return isVerified == true"
                actions:
                  - action: logger
                    message: "Both conditions met!"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TestEvent("admin", 5, true));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Both conditions met!"));
    }

    @Test
    void testConditionSkipsWhenScriptIsEmpty() {
        var yaml = """
                alias: lua-condition-empty
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: luaScriptCondition
                    script: ""
                actions:
                  - action: logger
                    message: "This should not appear"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TestEvent("admin", 5, true));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("This should not appear"));
    }

    @Test
    void testConditionWithScriptError() {
        var yaml = """
                alias: lua-condition-error
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: luaScriptCondition
                    script: "invalid lua {{"
                actions:
                  - action: logger
                    message: "This should not appear"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TestEvent("admin", 5, true));

        assertThatThrownBy(() -> engine.publishEvent(context))
                .hasCauseInstanceOf(RuntimeException.class);
    }
}
