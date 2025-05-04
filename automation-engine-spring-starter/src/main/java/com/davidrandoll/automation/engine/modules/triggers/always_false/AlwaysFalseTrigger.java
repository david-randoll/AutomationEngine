package com.davidrandoll.automation.engine.modules.triggers.always_false;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component("alwaysFalseTrigger")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "alwaysFalseTrigger", ignored = AlwaysFalseTrigger.class)
public class AlwaysFalseTrigger extends PluggableTrigger<AlwaysFalseTriggerContext> {

    @Override
    public boolean isTriggered(EventContext ec, AlwaysFalseTriggerContext tc) {
        return false;
    }
}
