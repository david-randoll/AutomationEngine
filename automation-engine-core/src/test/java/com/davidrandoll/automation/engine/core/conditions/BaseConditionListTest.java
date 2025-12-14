package com.davidrandoll.automation.engine.core.conditions;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.test.TestEvent;
import com.davidrandoll.automation.engine.test.mocks.SimpleCondition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class BaseConditionListTest {

    @Test
    void testOf_withVarargs_createsListWithConditions() {
        // Given
        SimpleCondition condition1 = new SimpleCondition("condition1");
        SimpleCondition condition2 = new SimpleCondition("condition2");

        // When
        BaseConditionList list = BaseConditionList.of(condition1, condition2);

        // Then
        assertThat(list).hasSize(2);
        assertThat(list).containsExactly(condition1, condition2);
    }

    @Test
    void testOf_withEmptyVarargs_createsEmptyList() {
        // When
        BaseConditionList list = BaseConditionList.of();

        // Then
        assertThat(list).isEmpty();
    }

    @Test
    void testOf_withList_createsListWithConditions() {
        // Given
        SimpleCondition condition1 = new SimpleCondition("condition1");
        SimpleCondition condition2 = new SimpleCondition("condition2");
        List<IBaseCondition> conditions = List.of(condition1, condition2);

        // When
        BaseConditionList list = BaseConditionList.of(conditions);

        // Then
        assertThat(list).hasSize(2);
        assertThat(list).containsExactly(condition1, condition2);
    }

    @Test
    void testAllSatisfied_whenAllConditionsTrue_returnsTrue() {
        // Given
        SimpleCondition condition1 = new SimpleCondition("condition1", true);
        SimpleCondition condition2 = new SimpleCondition("condition2", true);
        SimpleCondition condition3 = new SimpleCondition("condition3", true);
        BaseConditionList list = BaseConditionList.of(condition1, condition2, condition3);
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        boolean result = list.allSatisfied(context);

        // Then
        assertThat(result).isTrue();
        assertThat(condition1.getEvaluationCount()).isEqualTo(1);
        assertThat(condition2.getEvaluationCount()).isEqualTo(1);
        assertThat(condition3.getEvaluationCount()).isEqualTo(1);
    }

    @Test
    void testAllSatisfied_whenOneConditionFalse_returnsFalse() {
        // Given
        SimpleCondition condition1 = new SimpleCondition("condition1", true);
        SimpleCondition condition2 = new SimpleCondition("condition2", false);
        SimpleCondition condition3 = new SimpleCondition("condition3", true);
        BaseConditionList list = BaseConditionList.of(condition1, condition2, condition3);
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        boolean result = list.allSatisfied(context);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testAllSatisfied_withEmptyList_returnsTrue() {
        // Given
        BaseConditionList list = BaseConditionList.of();
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        boolean result = list.allSatisfied(context);

        // Then
        assertThat(result).isTrue(); // Empty stream.allMatch returns true
    }

    @Test
    void testAnySatisfied_whenOneConditionTrue_returnsTrue() {
        // Given
        SimpleCondition condition1 = new SimpleCondition("condition1", false);
        SimpleCondition condition2 = new SimpleCondition("condition2", true);
        SimpleCondition condition3 = new SimpleCondition("condition3", false);
        BaseConditionList list = BaseConditionList.of(condition1, condition2, condition3);
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        boolean result = list.anySatisfied(context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void testAnySatisfied_whenAllConditionsFalse_returnsFalse() {
        // Given
        SimpleCondition condition1 = new SimpleCondition("condition1", false);
        SimpleCondition condition2 = new SimpleCondition("condition2", false);
        BaseConditionList list = BaseConditionList.of(condition1, condition2);
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        boolean result = list.anySatisfied(context);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testAnySatisfied_withEmptyList_returnsFalse() {
        // Given
        BaseConditionList list = BaseConditionList.of();
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        boolean result = list.anySatisfied(context);

        // Then
        assertThat(result).isFalse(); // Empty stream.anyMatch returns false
    }

    @Test
    void testNoneSatisfied_whenAllConditionsFalse_returnsTrue() {
        // Given
        SimpleCondition condition1 = new SimpleCondition("condition1", false);
        SimpleCondition condition2 = new SimpleCondition("condition2", false);
        SimpleCondition condition3 = new SimpleCondition("condition3", false);
        BaseConditionList list = BaseConditionList.of(condition1, condition2, condition3);
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        boolean result = list.noneSatisfied(context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void testNoneSatisfied_whenOneConditionTrue_returnsFalse() {
        // Given
        SimpleCondition condition1 = new SimpleCondition("condition1", false);
        SimpleCondition condition2 = new SimpleCondition("condition2", true);
        SimpleCondition condition3 = new SimpleCondition("condition3", false);
        BaseConditionList list = BaseConditionList.of(condition1, condition2, condition3);
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        boolean result = list.noneSatisfied(context);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testNoneSatisfied_withEmptyList_returnsTrue() {
        // Given
        BaseConditionList list = BaseConditionList.of();
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        boolean result = list.noneSatisfied(context);

        // Then
        assertThat(result).isTrue(); // Empty stream.noneMatch returns true
    }

    @Test
    void testBaseConditionList_isArrayList() {
        // Given
        BaseConditionList list = new BaseConditionList();

        // Then - Verify it's a proper ArrayList
        assertThat(list).isInstanceOf(java.util.ArrayList.class);
        assertThat(list).isEmpty();

        // Can use ArrayList methods
        SimpleCondition condition = new SimpleCondition("condition");
        list.add(condition);
        assertThat(list).hasSize(1);
        assertThat(list.get(0)).isEqualTo(condition);
    }
}
