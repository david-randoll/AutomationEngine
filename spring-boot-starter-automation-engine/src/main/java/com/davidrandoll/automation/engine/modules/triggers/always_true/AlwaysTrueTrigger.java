package com.davidrandoll.automation.engine.modules.triggers.always_true;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableTrigger;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AlwaysTrueTrigger extends PluggableTrigger<AlwaysTrueTriggerContext> {

    @Override
    public boolean isTriggered(EventContext ec, AlwaysTrueTriggerContext tc) {
        return true;
    }
}
