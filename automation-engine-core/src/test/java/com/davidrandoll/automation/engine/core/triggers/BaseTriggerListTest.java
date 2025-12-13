package com.davidrandoll.automation.engine.core.triggers;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.test.TestEvent;
import com.davidrandoll.automation.engine.test.mocks.SimpleTrigger;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class BaseTriggerListTest {

    @Test
    void testOf_withVarargs_createsListWithTriggers() {
        // Given
        SimpleTrigger trigger1 = new SimpleTrigger("trigger1");
        SimpleTrigger trigger2 = new SimpleTrigger("trigger2");

        // When
        BaseTriggerList list = BaseTriggerList.of(trigger1, trigger2);

        // Then
        assertThat(list).hasSize(2);
        assertThat(list).containsExactly(trigger1, trigger2);
    }

    @Test
    void testOf_withEmptyVarargs_createsEmptyList() {
        // When
        BaseTriggerList list = BaseTriggerList.of();

        // Then
        assertThat(list).isEmpty();
    }

    @Test
    void testOf_withList_createsListWithTriggers() {
        // Given
        SimpleTrigger trigger1 = new SimpleTrigger("trigger1");
        SimpleTrigger trigger2 = new SimpleTrigger("trigger2");
        List<IBaseTrigger> triggers = List.of(trigger1, trigger2);

        // When
        BaseTriggerList list = BaseTriggerList.of(triggers);

        // Then
        assertThat(list).hasSize(2);
        assertThat(list).containsExactly(trigger1, trigger2);
    }

    @Test
    void testAllTriggered_whenAllTriggersActivated_returnsTrue() {
        // Given
        SimpleTrigger trigger1 = new SimpleTrigger("trigger1", true);
        SimpleTrigger trigger2 = new SimpleTrigger("trigger2", true);
        SimpleTrigger trigger3 = new SimpleTrigger("trigger3", true);
        BaseTriggerList list = BaseTriggerList.of(trigger1, trigger2, trigger3);
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        boolean result = list.allTriggered(context);

        // Then
        assertThat(result).isTrue();
        assertThat(trigger1.getCheckCount()).isEqualTo(1);
        assertThat(trigger2.getCheckCount()).isEqualTo(1);
        assertThat(trigger3.getCheckCount()).isEqualTo(1);
    }

    @Test
    void testAllTriggered_whenOneTriggerNotActivated_returnsFalse() {
        // Given
        SimpleTrigger trigger1 = new SimpleTrigger("trigger1", true);
        SimpleTrigger trigger2 = new SimpleTrigger("trigger2", false);
        SimpleTrigger trigger3 = new SimpleTrigger("trigger3", true);
        BaseTriggerList list = BaseTriggerList.of(trigger1, trigger2, trigger3);
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        boolean result = list.allTriggered(context);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testAllTriggered_withEmptyList_returnsTrue() {
        // Given
        BaseTriggerList list = BaseTriggerList.of();
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        boolean result = list.allTriggered(context);

        // Then
        assertThat(result).isTrue(); // Empty stream.allMatch returns true
    }

    @Test
    void testAnyTriggered_whenOneTriggerActivated_returnsTrue() {
        // Given
        SimpleTrigger trigger1 = new SimpleTrigger("trigger1", false);
        SimpleTrigger trigger2 = new SimpleTrigger("trigger2", true);
        SimpleTrigger trigger3 = new SimpleTrigger("trigger3", false);
        BaseTriggerList list = BaseTriggerList.of(trigger1, trigger2, trigger3);
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        boolean result = list.anyTriggered(context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void testAnyTriggered_whenAllTriggersNotActivated_returnsFalse() {
        // Given
        SimpleTrigger trigger1 = new SimpleTrigger("trigger1", false);
        SimpleTrigger trigger2 = new SimpleTrigger("trigger2", false);
        BaseTriggerList list = BaseTriggerList.of(trigger1, trigger2);
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        boolean result = list.anyTriggered(context);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testAnyTriggered_withEmptyList_returnsFalse() {
        // Given
        BaseTriggerList list = BaseTriggerList.of();
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        boolean result = list.anyTriggered(context);

        // Then
        assertThat(result).isFalse(); // Empty stream.anyMatch returns false
    }

    @Test
    void testNoneTriggered_whenAllTriggersNotActivated_returnsTrue() {
        // Given
        SimpleTrigger trigger1 = new SimpleTrigger("trigger1", false);
        SimpleTrigger trigger2 = new SimpleTrigger("trigger2", false);
        SimpleTrigger trigger3 = new SimpleTrigger("trigger3", false);
        BaseTriggerList list = BaseTriggerList.of(trigger1, trigger2, trigger3);
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        boolean result = list.noneTriggered(context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void testNoneTriggered_whenOneTriggerActivated_returnsFalse() {
        // Given
        SimpleTrigger trigger1 = new SimpleTrigger("trigger1", false);
        SimpleTrigger trigger2 = new SimpleTrigger("trigger2", true);
        SimpleTrigger trigger3 = new SimpleTrigger("trigger3", false);
        BaseTriggerList list = BaseTriggerList.of(trigger1, trigger2, trigger3);
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        boolean result = list.noneTriggered(context);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testNoneTriggered_withEmptyList_returnsTrue() {
        // Given
        BaseTriggerList list = BaseTriggerList.of();
        EventContext context = new EventContext(TestEvent.builder().eventType("TEST").build());

        // When
        boolean result = list.noneTriggered(context);

        // Then
        assertThat(result).isTrue(); // Empty stream.noneMatch returns true
    }

    @Test
    void testBaseTriggerList_isArrayList() {
        // Given
        BaseTriggerList list = new BaseTriggerList();

        // Then - Verify it's a proper ArrayList
        assertThat(list).isInstanceOf(java.util.ArrayList.class);
        assertThat(list).isEmpty();

        // Can use ArrayList methods
        SimpleTrigger trigger = new SimpleTrigger("trigger");
        list.add(trigger);
        assertThat(list).hasSize(1);
        assertThat(list.get(0)).isEqualTo(trigger);
    }
}
