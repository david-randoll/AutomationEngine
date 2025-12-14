package com.davidrandoll.automation.engine.creator;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.creator.actions.ActionBuilder;
import com.davidrandoll.automation.engine.creator.conditions.ConditionBuilder;
import com.davidrandoll.automation.engine.creator.parsers.AutomationParserRouter;
import com.davidrandoll.automation.engine.creator.parsers.ManualAutomationBuilder;
import com.davidrandoll.automation.engine.creator.parsers.yaml.YamlAutomationParser;
import com.davidrandoll.automation.engine.creator.result.ResultBuilder;
import com.davidrandoll.automation.engine.creator.triggers.TriggerBuilder;
import com.davidrandoll.automation.engine.creator.variables.VariableBuilder;
import com.davidrandoll.automation.engine.test.TestYamlConverter;
import com.davidrandoll.automation.engine.test.mocks.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests that exercise AutomationProcessor and all builder classes
 * by parsing YAML and creating Automation objects.
 */
class AutomationYamlIntegrationTest {

    private AutomationFactory factory;

    @BeforeEach
    void setUp() {
        var actionSupplier = new MockActionSupplier();
        actionSupplier.register("doNothing", new SimpleAction("doNothing"));

        var conditionSupplier = new MockConditionSupplier();
        conditionSupplier.register("alwaysTrue", new SimpleCondition("alwaysTrue", true));
        conditionSupplier.register("alwaysFalse", new SimpleCondition("alwaysFalse", false));

        var triggerSupplier = new MockTriggerSupplier();
        triggerSupplier.register("alwaysTrue", new SimpleTrigger("alwaysTrue", true));
        triggerSupplier.register("alwaysFalse", new SimpleTrigger("alwaysFalse", false));

        var variableSupplier = new MockVariableSupplier();
        variableSupplier.register("simpleVar", new SimpleVariable("simpleVar"));

        var resultSupplier = new MockResultSupplier();
        resultSupplier.addResult("message", new SimpleResult("message"));

        var actionBuilder = new ActionBuilder(actionSupplier, List.of());
        var conditionBuilder = new ConditionBuilder(conditionSupplier, List.of());
        var triggerBuilder = new TriggerBuilder(triggerSupplier, List.of());
        var variableBuilder = new VariableBuilder(variableSupplier, List.of());
        var resultBuilder = new ResultBuilder(resultSupplier, List.of());

        var processor = new AutomationProcessor(
                actionBuilder,
                conditionBuilder,
                triggerBuilder,
                variableBuilder,
                resultBuilder);

        var manualBuilder = new ManualAutomationBuilder(processor);
        var converter = new TestYamlConverter();
        var yamlParser = new YamlAutomationParser(manualBuilder, converter);

        var router = new AutomationParserRouter(Map.of("yamlAutomationParser", yamlParser));

        factory = new AutomationFactory(manualBuilder, router);
    }

    @Test
    void testParseSimpleAutomation() {
        // Given
        String yaml = """
                alias: simple-automation
                description: A simple test automation
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: doNothing
                """;

        // When
        Automation automation = factory.createAutomation("yaml", yaml);

        // Then
        assertThat(automation).isNotNull();
        assertThat(automation.getAlias()).isEqualTo("simple-automation");
    }

    @Test
    void testParseWithVariables() {
        // Given
        String yaml = """
                alias: automation-with-vars
                variables:
                  - variable: simpleVar
                    name: myVar
                    value: myValue
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: doNothing
                """;

        // When
        Automation automation = factory.createAutomation("yaml", yaml);

        // Then
        assertThat(automation).isNotNull();
        assertThat(automation.getVariables()).isNotNull();
    }

    @Test
    void testParseWithConditions() {
        // Given
        String yaml = """
                alias: automation-with-conditions
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: alwaysTrue
                actions:
                  - action: doNothing
                """;

        // When
        Automation automation = factory.createAutomation("yaml", yaml);

        // Then
        assertThat(automation).isNotNull();
        assertThat(automation.getConditions()).isNotNull();
    }

    @Test
    void testParseWithMultipleActions() {
        // Given
        String yaml = """
                alias: automation-multi-actions
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: doNothing
                  - action: doNothing
                  - action: doNothing
                """;

        // When
        Automation automation = factory.createAutomation("yaml", yaml);

        // Then
        assertThat(automation).isNotNull();
        assertThat(automation.getActions()).isNotNull();
    }

    @Test
    void testParseWithMultipleTriggers() {
        // Given
        String yaml = """
                alias: automation-multi-triggers
                triggers:
                  - trigger: alwaysTrue
                  - trigger: alwaysFalse
                actions:
                  - action: doNothing
                """;

        // When
        Automation automation = factory.createAutomation("yaml", yaml);

        // Then
        assertThat(automation).isNotNull();
        assertThat(automation.getTriggers()).isNotNull();
    }

    @Test
    void testParseWithResult() {
        // Given
        String yaml = """
                alias: automation-with-result
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: doNothing
                result:
                  message: "Done"
                """;

        // When
        Automation automation = factory.createAutomation("yaml", yaml);

        // Then
        assertThat(automation).isNotNull();
        assertThat(automation.getResult()).isNotNull();
    }

    @Test
    void testParseCompleteAutomation() {
        // Given
        String yaml = """
                alias: complete-automation
                description: A complete automation with all components
                variables:
                  - variable: simpleVar
                    name: var1
                    value: value1
                  - variable: simpleVar
                    name: var2
                    value: value2
                triggers:
                  - trigger: alwaysTrue
                  - trigger: alwaysFalse
                conditions:
                  - condition: alwaysTrue
                  - condition: alwaysFalse
                actions:
                  - action: doNothing
                  - action: doNothing
                result:
                  message: "Automation completed"
                """;

        // When
        Automation automation = factory.createAutomation("yaml", yaml);

        // Then
        assertThat(automation).isNotNull();
        assertThat(automation.getAlias()).isEqualTo("complete-automation");
        assertThat(automation.getVariables()).isNotNull();
        assertThat(automation.getTriggers()).isNotNull();
        assertThat(automation.getConditions()).isNotNull();
        assertThat(automation.getActions()).isNotNull();
        assertThat(automation.getResult()).isNotNull();
    }
}
