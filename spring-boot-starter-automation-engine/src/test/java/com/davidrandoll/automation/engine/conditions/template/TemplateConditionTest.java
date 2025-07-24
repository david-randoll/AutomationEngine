package com.davidrandoll.automation.engine.conditions.template;

import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.modules.conditions.template.TemplateCondition;
import com.davidrandoll.automation.engine.modules.conditions.template.TemplateConditionContext;
import com.davidrandoll.automation.engine.modules.events.DefaultEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateConditionTest extends AutomationEngineTest {

    @Test
    void testTemplateConditionIsSatisfiedWhenExpressionIsTrue() {
        var yaml = """
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: template
                    expression: "true"
                actions:
                  - action: logger
                    message: "Template condition activated!"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act
        engine.publishEvent(new DefaultEvent());

        // Assert
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Template condition activated!"));
    }

    @Test
    void testTemplateConditionIsNotSatisfiedWhenExpressionIsFalse() {
        var yaml = """
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: template
                    expression: "false"
                actions:
                  - action: logger
                    message: "Template condition activated!"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act
        engine.publishEvent(new DefaultEvent());

        // Assert
        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Template condition activated!"));
    }

    @Test
    void testTemplateTriggerHandlesNullExpressionGracefully() {
        TemplateConditionContext context = new TemplateConditionContext();
        context.setExpression(null);
        TemplateCondition condition = new TemplateCondition();

        boolean result = condition.isSatisfied(EventContext.of(new DefaultEvent()), context);

        assertThat(result).isFalse();
    }

    @Test
    void testTemplateConditionWithVariableAndPebbleExpression() {
        var yaml = """
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: template
                    expression: "{{ status == 'active' }}"
                actions:
                  - action: logger
                    message: "Template condition met!"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create an event and add a variable
        var event = EventContext.of(new DefaultEvent());
        event.addMetadata("status", "active");

        engine.publishEvent(event);

        // Assert: Ensure the action was logged
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Template condition met!"));
    }
}