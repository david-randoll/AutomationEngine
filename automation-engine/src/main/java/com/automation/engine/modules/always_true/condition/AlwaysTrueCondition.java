package com.automation.engine.modules.always_true.condition;

import com.automation.engine.core.conditions.AbstractCondition;
import com.automation.engine.core.events.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("alwaysTrueCondition")
@RequiredArgsConstructor
public class AlwaysTrueCondition extends AbstractCondition<AlwaysTrueConditionContext> {
    @Override
    public boolean isSatisfied(Event context, AlwaysTrueConditionContext conditionContext) {
        return true;
    }
}
