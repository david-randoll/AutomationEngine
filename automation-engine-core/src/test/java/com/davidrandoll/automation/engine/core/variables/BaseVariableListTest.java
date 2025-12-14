package com.davidrandoll.automation.engine.core.variables;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.test.TestEvent;
import com.davidrandoll.automation.engine.test.mocks.SimpleVariable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.*;

class BaseVariableListTest {

    @Test
    void testOf_withVarargs_createsListWithVariables() {
        // Given
        SimpleVariable var1 = new SimpleVariable("var1", "value1");
        SimpleVariable var2 = new SimpleVariable("var2", "value2");

        // When
        BaseVariableList list = BaseVariableList.of(var1, var2);

        // Then
        assertThat(list).hasSize(2);
        assertThat(list).containsExactly(var1, var2);
    }

    @Test
    void testOf_withEmptyVarargs_createsEmptyList() {
        // When
        BaseVariableList list = BaseVariableList.of();

        // Then
        assertThat(list).isEmpty();
    }

    @Test
    void testOf_withList_createsListWithVariables() {
        // Given
        SimpleVariable var1 = new SimpleVariable("var1", "value1");
        SimpleVariable var2 = new SimpleVariable("var2", "value2");
        List<IBaseVariable> variables = List.of(var1, var2);

        // When
        BaseVariableList list = BaseVariableList.of(variables);

        // Then
        assertThat(list).hasSize(2);
        assertThat(list).containsExactly(var1, var2);
    }

    @Test
    void testResolveAll_resolvesAllVariables() {
        // Given
        SimpleVariable var1 = new SimpleVariable("var1", "value1");
        SimpleVariable var2 = new SimpleVariable("var2", 42);
        SimpleVariable var3 = new SimpleVariable("var3", true);
        BaseVariableList list = BaseVariableList.of(var1, var2, var3);
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        list.resolveAll(context);

        // Then
        assertThat(var1.getResolveCount()).isEqualTo(1);
        assertThat(var2.getResolveCount()).isEqualTo(1);
        assertThat(var3.getResolveCount()).isEqualTo(1);
        assertThat(context.getMetadata()).containsEntry("var1", "value1");
        assertThat(context.getMetadata()).containsEntry("var2", 42);
        assertThat(context.getMetadata()).containsEntry("var3", true);
    }

    @Test
    void testResolveAll_withEmptyList_doesNotThrow() {
        // Given
        BaseVariableList list = BaseVariableList.of();
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When/Then
        assertThatCode(() -> list.resolveAll(context)).doesNotThrowAnyException();
    }

    @Test
    void testResolveAllAsync_resolvesAllVariables() {
        // Given
        SimpleVariable var1 = new SimpleVariable("var1", "value1");
        SimpleVariable var2 = new SimpleVariable("var2", 42);
        SimpleVariable var3 = new SimpleVariable("var3", true);
        BaseVariableList list = BaseVariableList.of(var1, var2, var3);
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        list.resolveAllAsync(context);

        // Then
        assertThat(var1.getResolveCount()).isEqualTo(1);
        assertThat(var2.getResolveCount()).isEqualTo(1);
        assertThat(var3.getResolveCount()).isEqualTo(1);
        assertThat(context.getMetadata()).containsEntry("var1", "value1");
        assertThat(context.getMetadata()).containsEntry("var2", 42);
        assertThat(context.getMetadata()).containsEntry("var3", true);
    }

    @Test
    void testResolveAllAsync_withEmptyList_doesNotThrow() {
        // Given
        BaseVariableList list = BaseVariableList.of();
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When/Then
        assertThatCode(() -> list.resolveAllAsync(context)).doesNotThrowAnyException();
    }

    @Test
    void testResolveAllAsync_withExecutor_resolvesAllVariables() {
        // Given
        SimpleVariable var1 = new SimpleVariable("var1", "value1");
        SimpleVariable var2 = new SimpleVariable("var2", 42);
        BaseVariableList list = BaseVariableList.of(var1, var2);
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            // When
            list.resolveAllAsync(context, executor);

            // Then
            assertThat(var1.getResolveCount()).isEqualTo(1);
            assertThat(var2.getResolveCount()).isEqualTo(1);
            assertThat(context.getMetadata()).containsEntry("var1", "value1");
            assertThat(context.getMetadata()).containsEntry("var2", 42);
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void testBaseVariableList_isArrayList() {
        // Given
        BaseVariableList list = new BaseVariableList();

        // Then - Verify it's a proper ArrayList
        assertThat(list).isInstanceOf(java.util.ArrayList.class);
        assertThat(list).isEmpty();

        // Can use ArrayList methods
        SimpleVariable variable = new SimpleVariable("var", "value");
        list.add(variable);
        assertThat(list).hasSize(1);
        assertThat(list.get(0)).isEqualTo(variable);
    }
}
