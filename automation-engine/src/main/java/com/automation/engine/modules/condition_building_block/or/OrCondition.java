package com.automation.engine.modules.condition_building_block.or;

import com.automation.engine.core.conditions.AbstractCondition;
import com.automation.engine.core.events.Event;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("orCondition")
public class OrCondition extends AbstractCondition<OrConditionContext> {

    @Override
    public boolean isSatisfied(Event context, OrConditionContext conditionContext) {
        if (ObjectUtils.isEmpty(conditionContext.getConditions())) return true;
        return conditionContext.getConditions().stream()
                .anyMatch(condition -> condition.isSatisfied(context));
    }
}