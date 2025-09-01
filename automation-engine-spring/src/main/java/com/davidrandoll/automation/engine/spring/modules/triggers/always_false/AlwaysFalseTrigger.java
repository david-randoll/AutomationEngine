package com.davidrandoll.automation.engine.spring.modules.triggers.always_false;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableTrigger;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AlwaysFalseTrigger extends PluggableTrigger<AlwaysFalseTriggerContext> {

    @Override
    public boolean isTriggered(EventContext ec, AlwaysFalseTriggerContext tc) {
        return false;
    }
}
