package com.automation.engine.modules.conditions.not;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import com.automation.engine.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("notCondition")
@RequiredArgsConstructor
public class NotCondition extends PluggableCondition<NotConditionContext> {

    @Override
    public boolean isSatisfied(EventContext eventContext, NotConditionContext conditionContext) {
        if (ObjectUtils.isEmpty(conditionContext.getConditions())) return true;
        return resolver.noneConditionSatisfied(eventContext, conditionContext.getConditions());
    }
}