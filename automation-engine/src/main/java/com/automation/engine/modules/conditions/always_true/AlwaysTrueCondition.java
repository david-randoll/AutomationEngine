package com.automation.engine.modules.conditions.always_true;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("alwaysTrueCondition")
@RequiredArgsConstructor
public class AlwaysTrueCondition extends PluggableCondition<AlwaysTrueConditionContext> {
    @Override
    public boolean isSatisfied(EventContext ec, AlwaysTrueConditionContext cc) {
        return true;
    }
}
