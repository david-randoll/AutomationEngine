package com.automation.engine.modules.action_building_block.delay;

import com.automation.engine.core.actions.AbstractAction;
import com.automation.engine.core.events.Event;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component("delayAction")
@RequiredArgsConstructor
public class DelayAction extends AbstractAction<DelayActionContext> {
    private final DefaultAutomationResolver resolver;

    @Override
    public void execute(Event event, DelayActionContext context) {
        Duration duration = context.getDuration();
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}