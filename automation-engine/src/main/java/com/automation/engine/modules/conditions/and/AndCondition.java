package com.automation.engine.modules.conditions.and;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("andCondition")
@RequiredArgsConstructor
public class AndCondition extends PluggableCondition<AndConditionContext> {

    @Override
    public boolean isSatisfied(EventContext eventContext, AndConditionContext conditionContext) {
        if (ObjectUtils.isEmpty(conditionContext.getConditions())) return true;
        return processor.allConditionsSatisfied(eventContext, conditionContext.getConditions());
    }
}