package com.davidrandoll.automation.engine.core.result;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.test.TestEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AutomationResultTest {

    @Test
    void testExecuted_createsExecutedResult() {
        // Given
        Automation automation = createSimpleAutomation();
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());
        Object executionSummary = "Automation completed successfully";

        // When
        AutomationResult result = AutomationResult.executed(automation, context, executionSummary);

        // Then
        assertThat(result.isExecuted()).isTrue();
        assertThat(result.getAutomation()).isNotNull();
        assertThat(result.getAutomation()).isNotSameAs(automation); // Should be a copy
        assertThat(result.getContext()).isEqualTo(context);
        assertThat(result.getResult()).isPresent();
        assertThat(result.getResult().get()).isEqualTo(executionSummary);
    }

    @Test
    void testExecuted_withNullResult() {
        // Given
        Automation automation = createSimpleAutomation();
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        AutomationResult result = AutomationResult.executed(automation, context, null);

        // Then
        assertThat(result.isExecuted()).isTrue();
        assertThat(result.getResult()).isEmpty();
    }

    @Test
    void testSkipped_createsSkippedResult() {
        // Given
        Automation automation = createSimpleAutomation();
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        AutomationResult result = AutomationResult.skipped(automation, context);

        // Then
        assertThat(result.isExecuted()).isFalse();
        assertThat(result.getAutomation()).isNotNull();
        assertThat(result.getContext()).isEqualTo(context);
        assertThat(result.getResult()).isEmpty();
    }

    @Test
    void testExecuted_withComplexResult() {
        // Given
        Automation automation = createSimpleAutomation();
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());
        var complexResult = new Object() {
            public String status = "success";
            public int code = 200;
        };

        // When
        AutomationResult result = AutomationResult.executed(automation, context, complexResult);

        // Then
        assertThat(result.getResult()).isPresent();
        assertThat(result.getResult().get()).isEqualTo(complexResult);
    }

    @Test
    void testResult_delegatesOptionalMethods() {
        // Given
        Automation automation = createSimpleAutomation();
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());
        String summary = "test result";

        // When - Using @Delegate methods
        AutomationResult resultWithValue = AutomationResult.executed(automation, context, summary);
        AutomationResult resultEmpty = AutomationResult.skipped(automation, context);

        // Then - Optional methods should be delegated
        assertThat(resultWithValue.isPresent()).isTrue();
        assertThat(resultWithValue.isEmpty()).isFalse();
        assertThat(resultWithValue.get()).isEqualTo(summary);
        assertThat(resultWithValue.orElse("default")).isEqualTo(summary);

        assertThat(resultEmpty.isPresent()).isFalse();
        assertThat(resultEmpty.isEmpty()).isTrue();
        assertThat(resultEmpty.orElse("default")).isEqualTo("default");
    }

    @Test
    void testAutomationCopy_isIndependent() {
        // Given
        Automation originalAutomation = createSimpleAutomation();
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        AutomationResult result = AutomationResult.executed(originalAutomation, context, "result");

        // Then
        assertThat(result.getAutomation()).isNotSameAs(originalAutomation);
        assertThat(result.getAutomation().getAlias()).isEqualTo(originalAutomation.getAlias());
    }

    private Automation createSimpleAutomation() {
        return new Automation("test-automation", null, null, null, null, null);
    }

    public Object getExecutionSummary() {
        return "summary";
    }
}
