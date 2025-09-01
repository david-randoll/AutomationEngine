package com.davidrandoll.automation.engine.modules.actions.delay;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableAction;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@RequiredArgsConstructor
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