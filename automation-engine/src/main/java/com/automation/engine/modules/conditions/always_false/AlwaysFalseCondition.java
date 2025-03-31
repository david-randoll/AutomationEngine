package com.automation.engine.modules.conditions.always_false;

import com.automation.engine.core.conditions.AbstractCondition;
import com.automation.engine.core.events.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("alwaysFalseCondition")
@RequiredArgsConstructor
public class AlwaysFalseCondition extends AbstractCondition<AlwaysFalseConditionContext> {
    @Override
    public boolean isSatisfied(Event event, AlwaysFalseConditionContext context) {
        return false;
    }
}