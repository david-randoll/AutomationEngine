package com.automation.engine.modules.always_false.trigger;

import com.automation.engine.core.events.Event;
import com.automation.engine.core.triggers.AbstractTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("alwaysFalseTrigger")
@RequiredArgsConstructor
public class AlwaysFalseTrigger extends AbstractTrigger<AlwaysFalseTriggerContext> {

    @Override
    public boolean isTriggered(Event event, AlwaysFalseTriggerContext context) {
        return false;
    }
}
