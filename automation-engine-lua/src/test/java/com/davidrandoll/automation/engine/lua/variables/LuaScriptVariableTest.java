package com.davidrandoll.automation.engine.lua.variables;

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
 * Integration tests for LuaScriptVariable.
 */
class LuaScriptVariableTest extends AutomationEngineTest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class UserEvent implements IEvent {
        private String firstName;
        private String lastName;
        private int age;
    }

    @Test
    void testBasicVariableComputation() {
        var yaml = """
                alias: lua-variable-basic
                variables:
                  - variable: luaScriptVariable
                    script: |
                      return {
                        greeting = "Hello, World!"
                      }
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "{{ greeting }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new UserEvent("John", "Doe", 30));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Hello, World!"));
    }

    @Test
    void testVariableUsingEventData() {
        var yaml = """
                alias: lua-variable-event-data
                variables:
                  - variable: luaScriptVariable
                    script: |
                      return {
                        fullName = event.firstName .. " " .. event.lastName,
                        isAdult = event.age >= 18
                      }
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "User: {{ fullName }}, Adult: {{ isAdult }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new UserEvent("John", "Doe", 30));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("User: John Doe, Adult: true"));
    }

    @Test
    void testVariableWithMathOperations() {
        var yaml = """
                alias: lua-variable-math
                variables:
                  - variable: luaScriptVariable
                    script: |
                      local birthYear = 2024 - event.age
                      return {
                        birthYear = birthYear,
                        nextBirthday = event.age + 1
                      }
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Birth year: {{ birthYear }}, Next age: {{ nextBirthday }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new UserEvent("John", "Doe", 30));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Birth year: 1994, Next age: 31"));
    }

    @Test
    void testVariableWithConditionalLogic() {
        var yaml = """
                alias: lua-variable-conditional
                variables:
                  - variable: luaScriptVariable
                    script: |
                      local ageGroup
                      if event.age < 18 then
                        ageGroup = "minor"
                      elseif event.age < 65 then
                        ageGroup = "adult"
                      else
                        ageGroup = "senior"
                      end
                      return {
                        ageGroup = ageGroup
                      }
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Age group: {{ ageGroup }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new UserEvent("John", "Doe", 30));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Age group: adult"));
    }

    @Test
    void testMultipleLuaVariables() {
        var yaml = """
                alias: lua-multiple-variables
                variables:
                  - variable: luaScriptVariable
                    script: |
                      return {
                        upperFirst = string.upper(event.firstName)
                      }
                  - variable: luaScriptVariable
                    script: |
                      return {
                        upperLast = string.upper(event.lastName)
                      }
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "{{ upperFirst }} {{ upperLast }}"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new UserEvent("John", "Doe", 30));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("JOHN DOE"));
    }

    @Test
    void testVariableSkipsWhenScriptIsEmpty() {
        var yaml = """
                alias: lua-variable-empty
                variables:
                  - variable: luaScriptVariable
                    script: ""
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "After empty variable"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new UserEvent("John", "Doe", 30));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("LuaScriptVariable skipped"));
    }

    @Test
    void testVariableWithScriptError() {
        var yaml = """
                alias: lua-variable-error
                variables:
                  - variable: luaScriptVariable
                    script: "invalid lua {{"
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "This should not appear"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new UserEvent("John", "Doe", 30));

        assertThatThrownBy(() -> engine.publishEvent(context))
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void testVariableReturnsEmptyResult() {
        var yaml = """
                alias: lua-variable-no-return
                variables:
                  - variable: luaScriptVariable
                    script: |
                      local x = 5
                      -- No return statement
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Completed"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new UserEvent("John", "Doe", 30));
        engine.publishEvent(context);

        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Completed"));
    }
}
