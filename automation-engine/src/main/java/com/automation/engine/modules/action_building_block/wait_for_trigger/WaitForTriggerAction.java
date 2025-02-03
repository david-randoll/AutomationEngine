package com.automation.engine.modules.action_building_block.wait_for_trigger;

import com.automation.engine.core.actions.AbstractAction;
import com.automation.engine.core.events.Event;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.time.Duration;
import java.util.Optional;

@Component("waitForTriggerAction")
@RequiredArgsConstructor
public class WaitForTriggerAction extends AbstractAction<WaitForTriggerActionContext> {
    private final DefaultAutomationResolver resolver;

    @Override
    public void execute(Event event, WaitForTriggerActionContext context) {
        if (ObjectUtils.isEmpty(context.getTriggers())) return;
        long timeout = Optional.ofNullable(context.getTimeout())
                .orElse(Duration.ofSeconds(5)).toMillis();
        long startTime = System.currentTimeMillis();

        // Loop until one of the triggers occurs or timeout is reached
        while (System.currentTimeMillis() - startTime < timeout) {
            var isTriggered = resolver.anyTriggersTriggered(event, context.getTriggers());
            if (isTriggered) return;

            // Sleep for a short time to avoid a busy-wait loop
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}