package com.automation.engine.modules.actions.delay;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import com.automation.engine.spi.AbstractAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component("delayAction")
@RequiredArgsConstructor
public class DelayAction extends AbstractAction<DelayActionContext> {
    private final DefaultAutomationResolver resolver;

    @Override
    public void execute(EventContext eventContext, DelayActionContext actionContext) {
        Duration duration = actionContext.getDuration();
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}