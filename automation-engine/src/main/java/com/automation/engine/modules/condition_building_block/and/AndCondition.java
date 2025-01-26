package com.automation.engine.modules.condition_building_block.and;

import com.automation.engine.core.conditions.AbstractCondition;
import com.automation.engine.core.events.Event;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("andCondition")
public class AndCondition extends AbstractCondition<AndConditionContext> {

    @Override
    public boolean isSatisfied(Event context, AndConditionContext conditionContext) {
        if (ObjectUtils.isEmpty(conditionContext.getConditions())) return true;
        return conditionContext.getConditions().stream()
                .allMatch(condition -> condition.isSatisfied(context));
    }
}