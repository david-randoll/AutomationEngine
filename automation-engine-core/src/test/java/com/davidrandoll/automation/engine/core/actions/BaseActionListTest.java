package com.davidrandoll.automation.engine.core.actions;

import com.davidrandoll.automation.engine.core.actions.exceptions.StopActionSequenceException;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.test.TestEvent;
import com.davidrandoll.automation.engine.test.mocks.SimpleAction;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.*;

class BaseActionListTest {

    @Test
    void testOf_withVarargs_createsListWithActions() {
        // Given
        SimpleAction action1 = new SimpleAction("action1");
        SimpleAction action2 = new SimpleAction("action2");

        // When
        BaseActionList list = BaseActionList.of(action1, action2);

        // Then
        assertThat(list).hasSize(2);
        assertThat(list).containsExactly(action1, action2);
    }

    @Test
    void testOf_withEmptyVarargs_createsEmptyList() {
        // When
        BaseActionList list = BaseActionList.of();

        // Then
        assertThat(list).isEmpty();
    }

    @Test
    void testOf_withList_createsListWithActions() {
        // Given
        SimpleAction action1 = new SimpleAction("action1");
        SimpleAction action2 = new SimpleAction("action2");
        List<IBaseAction> actions = List.of(action1, action2);

        // When
        BaseActionList list = BaseActionList.of(actions);

        // Then
        assertThat(list).hasSize(2);
        assertThat(list).containsExactly(action1, action2);
    }

    @Test
    void testExecuteAll_executesAllActions() {
        // Given
        SimpleAction action1 = new SimpleAction("action1");
        SimpleAction action2 = new SimpleAction("action2");
        SimpleAction action3 = new SimpleAction("action3");
        BaseActionList list = BaseActionList.of(action1, action2, action3);
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        list.executeAll(context);

        // Then
        assertThat(action1.getExecutionCount()).isEqualTo(1);
        assertThat(action2.getExecutionCount()).isEqualTo(1);
        assertThat(action3.getExecutionCount()).isEqualTo(1);
    }

    @Test
    void testExecuteAll_withEmptyList_doesNotThrow() {
        // Given
        BaseActionList list = BaseActionList.of();
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When/Then
        assertThatCode(() -> list.executeAll(context)).doesNotThrowAnyException();
    }

    @Test
    void testExecuteAll_catchesStopActionSequenceException() {
        // Given
        SimpleAction action1 = new SimpleAction("action1");
        action1.setExceptionToThrow(new StopActionSequenceException("Stop"));
        SimpleAction action2 = new SimpleAction("action2");
        BaseActionList list = BaseActionList.of(action1, action2);
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When/Then - Should not propagate exception
        assertThatCode(() -> list.executeAll(context)).doesNotThrowAnyException();

        // First action executes, second should not due to exception
        assertThat(action1.getExecutionCount()).isEqualTo(1);
        // Note: Second action won't execute if exception is thrown in first
    }

    @Test
    void testExecuteAll_propagatesOtherExceptions() {
        // Given
        SimpleAction action1 = new SimpleAction("action1");
        action1.setExceptionToThrow(new RuntimeException("Test exception"));
        BaseActionList list = BaseActionList.of(action1);
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When/Then - Should propagate non-StopActionSequenceException
        assertThatThrownBy(() -> list.executeAll(context))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Test exception");
    }

    @Test
    void testExecuteAllAsync_executesAllActions() {
        // Given
        SimpleAction action1 = new SimpleAction("action1");
        SimpleAction action2 = new SimpleAction("action2");
        SimpleAction action3 = new SimpleAction("action3");
        BaseActionList list = BaseActionList.of(action1, action2, action3);
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        list.executeAllAsync(context);

        // Then
        assertThat(action1.getExecutionCount()).isEqualTo(1);
        assertThat(action2.getExecutionCount()).isEqualTo(1);
        assertThat(action3.getExecutionCount()).isEqualTo(1);
    }

    @Test
    void testExecuteAllAsync_withEmptyList_doesNotThrow() {
        // Given
        BaseActionList list = BaseActionList.of();
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When/Then
        assertThatCode(() -> list.executeAllAsync(context)).doesNotThrowAnyException();
    }

    @Test
    void testExecuteAllAsync_withExecutor_executesAllActions() {
        // Given
        SimpleAction action1 = new SimpleAction("action1");
        SimpleAction action2 = new SimpleAction("action2");
        BaseActionList list = BaseActionList.of(action1, action2);
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            // When
            list.executeAllAsync(context, executor);

            // Then
            assertThat(action1.getExecutionCount()).isEqualTo(1);
            assertThat(action2.getExecutionCount()).isEqualTo(1);
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void testExecuteAllAsync_catchesStopActionSequenceException() {
        // Given
        SimpleAction action1 = new SimpleAction("action1");
        action1.setExceptionToThrow(new StopActionSequenceException("Stop"));
        BaseActionList list = BaseActionList.of(action1);
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When/Then - StopActionSequenceException should be caught and not propagate
        assertThatCode(() -> list.executeAllAsync(context))
                .doesNotThrowAnyException();
    }

    @Test
    void testBaseActionList_isArrayList() {
        // Given
        BaseActionList list = new BaseActionList();

        // Then - Verify it's a proper ArrayList
        assertThat(list).isInstanceOf(java.util.ArrayList.class);
        assertThat(list).isEmpty();

        // Can use ArrayList methods
        SimpleAction action = new SimpleAction("action");
        list.add(action);
        assertThat(list).hasSize(1);
        assertThat(list.get(0)).isEqualTo(action);
    }
}
