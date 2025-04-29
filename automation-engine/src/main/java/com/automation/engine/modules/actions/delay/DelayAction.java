package com.automation.engine.modules.actions.delay;

import com.automation.engine.conditional.AEConditionalOnMissingBeanName;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component("delayAction")
@RequiredArgsConstructor
@AEConditionalOnMissingBeanName("delayAction")
public class DelayAction extends PluggableAction<DelayActionContext> {

    @Override
    public void execute(EventContext ec, DelayActionContext ac) {
        Duration duration = ac.getDuration();
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}