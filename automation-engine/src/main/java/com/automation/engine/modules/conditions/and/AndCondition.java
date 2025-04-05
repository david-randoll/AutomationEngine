package com.automation.engine.modules.conditions.and;

import com.automation.engine.core.conditions.AbstractCondition;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("andCondition")
@RequiredArgsConstructor
public class AndCondition extends AbstractCondition<AndConditionContext> {
    private final DefaultAutomationResolver resolver;

    @Override
    public boolean isSatisfied(EventContext eventContext, AndConditionContext conditionContext) {
        if (ObjectUtils.isEmpty(conditionContext.getConditions())) return true;
        return resolver.allConditionsSatisfied(eventContext, conditionContext.getConditions());
    }
}