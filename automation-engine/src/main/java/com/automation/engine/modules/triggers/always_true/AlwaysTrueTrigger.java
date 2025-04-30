package com.automation.engine.modules.triggers.always_true;

import com.automation.engine.conditional.AEConditionalOnMissingBeanName;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.PluggableTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("alwaysTrueTrigger")
@RequiredArgsConstructor
@AEConditionalOnMissingBeanName
public class AlwaysTrueTrigger extends PluggableTrigger<AlwaysTrueTriggerContext> {

    @Override
    public boolean isTriggered(EventContext ec, AlwaysTrueTriggerContext tc) {
        return true;
    }
}
