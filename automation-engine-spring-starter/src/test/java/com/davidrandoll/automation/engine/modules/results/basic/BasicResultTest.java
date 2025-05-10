package com.davidrandoll.automation.engine.modules.results.basic;

import com.davidrandoll.automation.engine.AutomationEngineTest;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import com.davidrandoll.automation.engine.modules.events.time_based.TimeBasedEvent;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BasicResultTest extends AutomationEngineTest {

    @Test
    void testRunAutomation_withCustomResult() {
        var yaml = """
                alias: return-custom-result
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Executing actions..."
                result:
                  alias: result-alias
                  success: true
                  message: "Operation completed"
                  recordId: 12345
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));

        AutomationResult result = engine.runAutomation(automation, eventContext);
        assertThat(result.isExecuted()).isTrue();
        assertThat(result.getResult())
                .isPresent().get()
                .asInstanceOf(InstanceOfAssertFactories.MAP)
                .containsEntry("success", true)
                .containsEntry("message", "Operation completed")
                .containsEntry("recordId", 12345);
    }

    @Test
    void testRunAutomation_withSingleResultField() {
        var yaml = """
                alias: single-result
                triggers:
                  - trigger: alwaysTrue
                actions: []
                result:
                  status: "ok"
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();
        assertThat(result.getResult())
                .isPresent().get()
                .asInstanceOf(InstanceOfAssertFactories.MAP)
                .containsEntry("status", "ok");
    }

    @Test
    void testRunAutomation_withNestedResultFields() {
        var yaml = """
                alias: nested-result
                triggers:
                  - trigger: alwaysTrue
                actions: []
                result:
                  success: true
                  details:
                    createdAt: "2025-05-10T12:00:00Z"
                    createdBy: "admin"
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();
        assertThat(result.getResult())
                .isPresent().get()
                .asInstanceOf(InstanceOfAssertFactories.MAP)
                .containsEntry("success", true)
                .containsKey("details");

        var details = (Map<String, Object>) ((Map<?, ?>) result.getResult().get()).get("details");
        assertThat(details)
                .containsEntry("createdAt", "2025-05-10T12:00:00Z")
                .containsEntry("createdBy", "admin");
    }

    @Test
    void testRunAutomation_withListResultField() {
        var yaml = """
                alias: list-result
                triggers:
                  - trigger: alwaysTrue
                actions: []
                result:
                  items:
                    - "item1"
                    - "item2"
                    - "item3"
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();
        assertThat(result.getResult())
                .isPresent().get()
                .asInstanceOf(InstanceOfAssertFactories.MAP)
                .containsKey("items");

        var items = (List<String>) ((Map<?, ?>) result.getResult().get()).get("items");
        assertThat(items)
                .containsExactly("item1", "item2", "item3");
    }

    @Test
    void testRunAutomation_withMixedDataTypesInResult() {
        var yaml = """
                alias: mixed-result
                triggers:
                  - trigger: alwaysTrue
                actions: []
                result:
                  count: 42
                  active: false
                  username: "david"
                  tags:
                    - "tag1"
                    - "tag2"
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();
        var resultMap = (Map<String, Object>) result.getResult().get();

        assertThat(resultMap)
                .containsEntry("count", 42)
                .containsEntry("active", false)
                .containsEntry("username", "david");

        var tags = (List<String>) resultMap.get("tags");
        assertThat(tags)
                .containsExactly("tag1", "tag2");
    }

    @Test
    void testRunAutomation_withEmptyResult() {
        var yaml = """
                alias: empty-result
                triggers:
                  - trigger: alwaysTrue
                actions: []
                result: {}
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();
        assertThat(result.getResult())
                .isPresent().get()
                .asInstanceOf(InstanceOfAssertFactories.MAP)
                .isEmpty();
    }

    @Test
    void testRunAutomation_skipped_noResult() {
        var yaml = """
                alias: skipped-automation
                triggers:
                  - trigger: alwaysFalse
                actions:
                  - action: logger
                    message: "You should not see this."
                result:
                  success: true
                  message: "This should not appear."
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isFalse();
        assertThat(result.getResult()).isEmpty();
    }

    @Test
    void testRunAutomation_skipped_withMinimalYaml() {
        var yaml = """
                alias: minimal-skipped
                triggers:
                  - trigger: alwaysFalse
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isFalse();
        assertThat(result.getResult()).isEmpty();
    }


}