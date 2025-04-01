package com.automation.engine.modules.conditions.always_true;

import com.automation.engine.core.conditions.AbstractCondition;
import com.automation.engine.core.events.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("alwaysTrueCondition")
@RequiredArgsConstructor
public class AlwaysTrueCondition extends AbstractCondition<AlwaysTrueConditionContext> {
    @Override
    public boolean isSatisfied(Event event, AlwaysTrueConditionContext context) {
        return true;
    }
}
