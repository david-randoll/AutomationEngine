package com.automation.engine.modules.conditions.always_false;

import com.automation.engine.core.conditions.AbstractCondition;
import com.automation.engine.core.events.EventContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("alwaysFalseCondition")
@RequiredArgsConstructor
public class AlwaysFalseCondition extends AbstractCondition<AlwaysFalseConditionContext> {
    @Override
    public boolean isSatisfied(EventContext eventContext, AlwaysFalseConditionContext conditionContext) {
        return false;
    }
}