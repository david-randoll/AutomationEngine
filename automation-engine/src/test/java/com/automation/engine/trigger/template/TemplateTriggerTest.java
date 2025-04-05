package com.automation.engine.trigger.template;

import ch.qos.logback.classic.Logger;
import com.automation.engine.AutomationEngineApplication;
import com.automation.engine.TestLogAppender;
import com.automation.engine.core.Automation;
import com.automation.engine.core.AutomationEngine;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.factory.AutomationFactory;
import com.automation.engine.modules.events.DefaultEvent;
import com.automation.engine.modules.triggers.template.TemplateTrigger;
import com.automation.engine.modules.triggers.template.TemplateTriggerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AutomationEngineApplication.class)
@ExtendWith(SpringExtension.class)
class TemplateTriggerTest {
    @Autowired
    private AutomationEngine engine;

    @Autowired
    private AutomationFactory factory;

    private TestLogAppender logAppender;

    @BeforeEach
    void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger("com.automation.engine");
        logAppender = new TestLogAppender();
        logger.addAppender(logAppender);
        logAppender.start();

        engine.clearAutomations();
    }

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
        engine.addAutomation(automation);

        // Act
        engine.processEvent(new DefaultEvent());

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
        engine.addAutomation(automation);

        // Act
        engine.processEvent(new DefaultEvent());

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
        engine.addAutomation(automation);

        // Act: Create an event and add a variable
        var event = EventContext.of(new DefaultEvent());
        event.addMetadata("status", "active");

        engine.processEvent(event);

        // Assert: Ensure the action was logged
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Template trigger met!"));
    }
}