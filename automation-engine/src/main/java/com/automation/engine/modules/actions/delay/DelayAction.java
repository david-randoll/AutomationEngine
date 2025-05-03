package com.automation.engine.modules.actions.delay;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component("delayAction")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "delayAction", ignored = DelayAction.class)
public class DelayAction extends PluggableAction<DelayActionContext> {

    @Override
    public void doExecute(EventContext ec, DelayActionContext ac) {
        Duration duration = ac.getDuration();
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}