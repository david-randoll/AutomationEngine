package com.davidrandoll.automation.engine.modules.conditions.always_true;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AlwaysTrueCondition extends PluggableCondition<AlwaysTrueConditionContext> {
    @Override
    public boolean isSatisfied(EventContext ec, AlwaysTrueConditionContext cc) {
        return true;
    }
}
