package com.automation.engine.conditions.template;

import ch.qos.logback.classic.Logger;
import com.automation.engine.AutomationEngineApplication;
import com.automation.engine.TestLogAppender;
import com.automation.engine.core.Automation;
import com.automation.engine.core.AutomationEngine;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.factory.AutomationFactory;
import com.automation.engine.modules.conditions.template.TemplateCondition;
import com.automation.engine.modules.conditions.template.TemplateConditionContext;
import com.automation.engine.modules.events.DefaultEvent;
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
class TemplateConditionTest {
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

        engine.removeAll();
    }

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