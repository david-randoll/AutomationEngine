package com.davidrandoll.automation.engine.modules.triggers.always_true;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component("alwaysTrueTrigger")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "alwaysTrueTrigger", ignored = AlwaysTrueTrigger.class)
public class AlwaysTrueTrigger extends PluggableTrigger<AlwaysTrueTriggerContext> {

    @Override
    public boolean isTriggered(EventContext ec, AlwaysTrueTriggerContext tc) {
        return true;
    }
}
