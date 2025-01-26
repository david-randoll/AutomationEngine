package com.automation.engine.modules.condition_building_block.not;

import com.automation.engine.core.conditions.AbstractCondition;
import com.automation.engine.core.events.Event;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("notCondition")
public class NotCondition extends AbstractCondition<NotConditionContext> {

    @Override
    public boolean isSatisfied(Event context, NotConditionContext conditionContext) {
        if (ObjectUtils.isEmpty(conditionContext.getConditions())) return true;
        return conditionContext.getConditions().stream()
                .noneMatch(condition -> condition.isSatisfied(context));
    }
}