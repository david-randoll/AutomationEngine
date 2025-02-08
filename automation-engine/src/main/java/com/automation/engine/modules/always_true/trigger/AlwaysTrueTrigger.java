package com.automation.engine.modules.always_true.trigger;

import com.automation.engine.core.events.Event;
import com.automation.engine.core.triggers.AbstractTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("alwaysTrueTrigger")
@RequiredArgsConstructor
public class AlwaysTrueTrigger extends AbstractTrigger<AlwaysTrueTriggerContext> {

    @Override
    public boolean isTriggered(Event event, AlwaysTrueTriggerContext context) {
        return true;
    }
}
