package com.davidrandoll.automation.engine.trigger.template;

import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.modules.events.DefaultEvent;
import com.davidrandoll.automation.engine.modules.triggers.template.TemplateTrigger;
import com.davidrandoll.automation.engine.modules.triggers.template.TemplateTriggerContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateTriggerTest extends AutomationEngineTest {

    @Test
    void testTemplateTriggerIsTriggeredWhenExpressionIsTrue() {
        var yaml = """
                alias: Template Trigger Test
                triggers:
                  - trigger: template
                    expression: "true"
                actions:
                  - action: logger
                    message: "Template trigger activated!"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act
        engine.publishEvent(new DefaultEvent());

        // Assert
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Template trigger activated!"));
    }

    @Test
    void testTemplateTriggerIsNotTriggeredWhenExpressionIsFalse() {
        var yaml = """
                alias: Template Trigger Test
                triggers:
                  - trigger: template
                    expression: "false"
                actions:
                  - action: logger
                    message: "Template trigger activated!"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act
        engine.publishEvent(new DefaultEvent());

        // Assert
        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("Template trigger activated!"));
    }

    @Test
    void testTemplateTriggerHandlesNullExpressionGracefully() {
        TemplateTriggerContext context = new TemplateTriggerContext();
        context.setExpression(null);
        TemplateTrigger trigger = new TemplateTrigger();
        boolean result = trigger.isTriggered(EventContext.of(new DefaultEvent()), context);

        assertThat(result).isFalse();
    }

    @Test
    void testTemplateTriggerWithVariableAndPebbleExpression() {
        var yaml = """
                alias: Template Trigger with Variable
                triggers:
                  - trigger: template
                    expression: "{{ status == 'active' }}"
                actions:
                  - action: logger
                    message: "Template trigger met!"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act: Create an event and add a variable
        var event = EventContext.of(new DefaultEvent());
        event.addMetadata("status", "active");

        engine.publishEvent(event);

        // Assert: Ensure the action was logged
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Template trigger met!"));
    }
}