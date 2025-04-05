package com.automation.engine.modules.triggers.always_true;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.AbstractTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("alwaysTrueTrigger")
@RequiredArgsConstructor
public class AlwaysTrueTrigger extends AbstractTrigger<AlwaysTrueTriggerContext> {

    @Override
    public boolean isTriggered(EventContext eventContext, AlwaysTrueTriggerContext triggerContext) {
        return true;
    }
}
