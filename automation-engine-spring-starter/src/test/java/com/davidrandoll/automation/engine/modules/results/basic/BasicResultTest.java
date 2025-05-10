package com.davidrandoll.automation.engine.modules.results.basic;

import com.davidrandoll.automation.engine.AutomationEngineTest;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import com.davidrandoll.automation.engine.modules.events.time_based.TimeBasedEvent;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

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
}