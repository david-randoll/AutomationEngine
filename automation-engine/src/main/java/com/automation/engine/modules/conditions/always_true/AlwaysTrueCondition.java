package com.automation.engine.modules.conditions.always_true;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.AbstractCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("alwaysTrueCondition")
@RequiredArgsConstructor
public class AlwaysTrueCondition extends AbstractCondition<AlwaysTrueConditionContext> {
    @Override
    public boolean isSatisfied(EventContext eventContext, AlwaysTrueConditionContext conditionContext) {
        return true;
    }
}
