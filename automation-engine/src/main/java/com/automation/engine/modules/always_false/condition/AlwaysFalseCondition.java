package com.automation.engine.modules.always_false.condition;

import com.automation.engine.core.conditions.AbstractCondition;
import com.automation.engine.core.events.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("alwaysFalseCondition")
@RequiredArgsConstructor
public class AlwaysFalseCondition extends AbstractCondition<AlwaysFalseConditionContext> {
    @Override
    public boolean isSatisfied(Event context, AlwaysFalseConditionContext conditionContext) {
        return false;
    }
}