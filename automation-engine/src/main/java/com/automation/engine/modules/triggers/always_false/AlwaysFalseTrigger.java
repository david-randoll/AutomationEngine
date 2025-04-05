package com.automation.engine.modules.triggers.always_false;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.core.triggers.BaseAbstractTrigger;
import com.automation.engine.spi.AbstractTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("alwaysFalseTrigger")
@RequiredArgsConstructor
public class AlwaysFalseTrigger extends AbstractTrigger<AlwaysFalseTriggerContext> {

    @Override
    public boolean isTriggered(EventContext eventContext, AlwaysFalseTriggerContext triggerContext) {
        return false;
    }
}
