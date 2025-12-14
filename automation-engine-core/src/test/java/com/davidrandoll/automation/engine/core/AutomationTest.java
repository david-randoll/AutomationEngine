package com.davidrandoll.automation.engine.core;

import com.davidrandoll.automation.engine.core.actions.BaseActionList;
import com.davidrandoll.automation.engine.core.actions.exceptions.StopAutomationException;
import com.davidrandoll.automation.engine.core.conditions.BaseConditionList;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.IBaseResult;
import com.davidrandoll.automation.engine.core.triggers.BaseTriggerList;
import com.davidrandoll.automation.engine.core.variables.BaseVariableList;
import com.davidrandoll.automation.engine.test.TestEvent;
import com.davidrandoll.automation.engine.test.mocks.SimpleAction;
import com.davidrandoll.automation.engine.test.mocks.SimpleCondition;
import com.davidrandoll.automation.engine.test.mocks.SimpleTrigger;
import com.davidrandoll.automation.engine.test.mocks.SimpleVariable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class AutomationTest {

    @Test
    void testConstructor_withAllParameters() {
        // Given
        String alias = "test-automation";
        BaseVariableList variables = BaseVariableList.of(new SimpleVariable("var1", "value1"));
        BaseTriggerList triggers = BaseTriggerList.of(new SimpleTrigger("trigger1"));
        BaseConditionList conditions = BaseConditionList.of(new SimpleCondition("condition1"));
        BaseActionList actions = BaseActionList.of(new SimpleAction("action1"));
        IBaseResult result = context -> "result";

        // When
        Automation automation = new Automation(alias, variables, triggers, conditions, actions, result);

        // Then
        assertThat(automation.getAlias()).isEqualTo(alias);
        assertThat(automation.getVariables()).isEqualTo(variables);
        assertThat(automation.getTriggers()).isEqualTo(triggers);
        assertThat(automation.getConditions()).isEqualTo(conditions);
        assertThat(automation.getActions()).isEqualTo(actions);
        assertThat(automation.getResult()).isEqualTo(result);
    }

    @Test
    void testConstructor_withNullParameters_usesDefaults() {
        // When
        Automation automation = new Automation("test", null, null, null, null, null);

        // Then
        assertThat(automation.getAlias()).isEqualTo("test");
        assertThat(automation.getVariables()).isNotNull().isEmpty();
        assertThat(automation.getTriggers()).isNotNull().isEmpty();
        assertThat(automation.getConditions()).isNotNull().isEmpty();
        assertThat(automation.getActions()).isNotNull().isEmpty();
        assertThat(automation.getResult()).isNotNull();

        // Verify default result returns null
        EventContext context = new EventContext(new TestEvent());
        assertThat(automation.getExecutionSummary(context)).isNull();
    }

    @Test
    void testCopyConstructor_createsIndependentCopy() {
        // Given
        SimpleAction action = new SimpleAction("action1");
        SimpleCondition condition = new SimpleCondition("condition1");
        SimpleTrigger trigger = new SimpleTrigger("trigger1");
        SimpleVariable variable = new SimpleVariable("var1", "value1");

        Automation original = new Automation(
                "original",
                BaseVariableList.of(variable),
                BaseTriggerList.of(trigger),
                BaseConditionList.of(condition),
                BaseActionList.of(action),
                context -> "result");

        // When
        Automation copy = new Automation(original);

        // Then
        assertThat(copy.getAlias()).isEqualTo(original.getAlias());
        assertThat(copy.getVariables()).isNotSameAs(original.getVariables());
        assertThat(copy.getTriggers()).isNotSameAs(original.getTriggers());
        assertThat(copy.getConditions()).isNotSameAs(original.getConditions());
        assertThat(copy.getActions()).isNotSameAs(original.getActions());
        assertThat(copy.getResult()).isSameAs(original.getResult()); // Result is shared
    }

    @Test
    void testResolveVariables_resolvesAllVariables() {
        // Given
        SimpleVariable var1 = new SimpleVariable("var1", "value1");
        SimpleVariable var2 = new SimpleVariable("var2", 42);
        BaseVariableList variables = BaseVariableList.of(var1, var2);

        Automation automation = new Automation("test", variables, null, null, null, null);
        EventContext context = new EventContext(new TestEvent());

        // When
        automation.resolveVariables(context);

        // Then
        assertThat(var1.getResolveCount()).isEqualTo(1);
        assertThat(var2.getResolveCount()).isEqualTo(1);
        assertThat(context.getMetadata()).containsEntry("var1", "value1");
        assertThat(context.getMetadata()).containsEntry("var2", 42);
    }

    @Test
    void testResolveVariables_withNullVariables_doesNotThrow() {
        // Given
        Automation automation = new Automation("test", null, null, null, null, null);
        EventContext context = new EventContext(new TestEvent());

        // When/Then - should not throw
        assertThatCode(() -> automation.resolveVariables(context)).doesNotThrowAnyException();
    }

    @Test
    void testAnyTriggerActivated_whenOneTriggerActivated_returnsTrue() {
        // Given
        SimpleTrigger trigger1 = new SimpleTrigger("trigger1", false);
        SimpleTrigger trigger2 = new SimpleTrigger("trigger2", true);
        BaseTriggerList triggers = BaseTriggerList.of(trigger1, trigger2);

        Automation automation = new Automation("test", null, triggers, null, null, null);
        EventContext context = new EventContext(new TestEvent());

        // When
        boolean result = automation.anyTriggerActivated(context);

        // Then
        assertThat(result).isTrue();
        assertThat(trigger1.getCheckCount()).isEqualTo(1);
        assertThat(trigger2.getCheckCount()).isEqualTo(1);
    }

    @Test
    void testAnyTriggerActivated_whenNoTriggersActivated_returnsFalse() {
        // Given
        SimpleTrigger trigger1 = new SimpleTrigger("trigger1", false);
        SimpleTrigger trigger2 = new SimpleTrigger("trigger2", false);
        BaseTriggerList triggers = BaseTriggerList.of(trigger1, trigger2);

        Automation automation = new Automation("test", null, triggers, null, null, null);
        EventContext context = new EventContext(new TestEvent());

        // When
        boolean result = automation.anyTriggerActivated(context);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testAnyTriggerActivated_withNullTriggers_returnsFalse() {
        // Given
        Automation automation = new Automation("test", null, null, null, null, null);
        EventContext context = new EventContext(new TestEvent());

        // When
        boolean result = automation.anyTriggerActivated(context);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testAllConditionsMet_whenAllConditionsSatisfied_returnsTrue() {
        // Given
        SimpleCondition condition1 = new SimpleCondition("condition1", true);
        SimpleCondition condition2 = new SimpleCondition("condition2", true);
        BaseConditionList conditions = BaseConditionList.of(condition1, condition2);

        Automation automation = new Automation("test", null, null, conditions, null, null);
        EventContext context = new EventContext(new TestEvent());

        // When
        boolean result = automation.allConditionsMet(context);

        // Then
        assertThat(result).isTrue();
        assertThat(condition1.getEvaluationCount()).isEqualTo(1);
        assertThat(condition2.getEvaluationCount()).isEqualTo(1);
    }

    @Test
    void testAllConditionsMet_whenOneConditionNotSatisfied_returnsFalse() {
        // Given
        SimpleCondition condition1 = new SimpleCondition("condition1", true);
        SimpleCondition condition2 = new SimpleCondition("condition2", false);
        BaseConditionList conditions = BaseConditionList.of(condition1, condition2);

        Automation automation = new Automation("test", null, null, conditions, null, null);
        EventContext context = new EventContext(new TestEvent());

        // When
        boolean result = automation.allConditionsMet(context);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testAllConditionsMet_withNullConditions_returnsTrue() {
        // Given
        Automation automation = new Automation("test", null, null, null, null, null);
        EventContext context = new EventContext(new TestEvent());

        // When
        boolean result = automation.allConditionsMet(context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void testPerformActions_executesAllActions() {
        // Given
        SimpleAction action1 = new SimpleAction("action1");
        SimpleAction action2 = new SimpleAction("action2");
        BaseActionList actions = BaseActionList.of(action1, action2);

        Automation automation = new Automation("test", null, null, null, actions, null);
        EventContext context = new EventContext(new TestEvent());

        // When
        automation.performActions(context);

        // Then
        assertThat(action1.getExecutionCount()).isEqualTo(1);
        assertThat(action2.getExecutionCount()).isEqualTo(1);
    }

    @Test
    void testPerformActions_withNullActions_doesNotThrow() {
        // Given
        Automation automation = new Automation("test", null, null, null, null, null);
        EventContext context = new EventContext(new TestEvent());

        // When/Then - should not throw
        assertThatCode(() -> automation.performActions(context)).doesNotThrowAnyException();
    }

    @Test
    void testPerformActions_catchesStopAutomationException() {
        // Given
        SimpleAction action1 = new SimpleAction("action1");
        action1.setExceptionToThrow(new StopAutomationException("Stop"));
        SimpleAction action2 = new SimpleAction("action2");
        BaseActionList actions = BaseActionList.of(action1, action2);

        Automation automation = new Automation("test", null, null, null, actions, null);
        EventContext context = new EventContext(new TestEvent());

        // When/Then - should not propagate exception
        assertThatCode(() -> automation.performActions(context)).doesNotThrowAnyException();

        // First action should execute, second should not due to StopAutomationException
        assertThat(action1.getExecutionCount()).isEqualTo(1);
    }

    @Test
    void testGetExecutionSummary_returnsResultFromResult() {
        // Given
        String expectedSummary = "execution-summary";
        IBaseResult result = context -> expectedSummary;

        Automation automation = new Automation("test", null, null, null, null, result);
        EventContext context = new EventContext(new TestEvent());

        // When
        Object summary = automation.getExecutionSummary(context);

        // Then
        assertThat(summary).isEqualTo(expectedSummary);
    }

    @Test
    void testGetExecutionSummary_withDefaultResult_returnsNull() {
        // Given
        Automation automation = new Automation("test", null, null, null, null, null);
        EventContext context = new EventContext(new TestEvent());

        // When
        Object summary = automation.getExecutionSummary(context);

        // Then
        assertThat(summary).isNull();
    }

    @Test
    void testFullAutomationFlow() {
        // Given - Create a complete automation
        SimpleVariable var1 = new SimpleVariable("userId", 123);
        SimpleTrigger trigger = new SimpleTrigger("userEvent", true);
        SimpleCondition condition = new SimpleCondition("isActive", true);
        SimpleAction action = new SimpleAction("sendEmail");
        IBaseResult result = context -> "Email sent to user " + context.getMetadata().get("userId");

        Automation automation = new Automation(
                "user-notification",
                BaseVariableList.of(var1),
                BaseTriggerList.of(trigger),
                BaseConditionList.of(condition),
                BaseActionList.of(action),
                result);

        TestEvent event = TestEvent.builder().eventType("USER_LOGIN").message("User logged in").build();
        EventContext context = new EventContext(event);

        // When - Execute full automation flow
        automation.resolveVariables(context);
        boolean triggered = automation.anyTriggerActivated(context);
        boolean conditionsMet = automation.allConditionsMet(context);

        if (triggered && conditionsMet) {
            automation.performActions(context);
        }

        Object summary = automation.getExecutionSummary(context);

        // Then
        assertThat(triggered).isTrue();
        assertThat(conditionsMet).isTrue();
        assertThat(action.getExecutionCount()).isEqualTo(1);
        assertThat(summary).isEqualTo("Email sent to user 123");
        assertThat(context.getMetadata()).containsEntry("userId", 123);
    }
}
