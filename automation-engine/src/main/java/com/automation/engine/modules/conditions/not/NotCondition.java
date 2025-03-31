package com.automation.engine.modules.conditions.not;

import com.automation.engine.core.conditions.AbstractCondition;
import com.automation.engine.core.events.Event;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("notCondition")
@RequiredArgsConstructor
public class NotCondition extends AbstractCondition<NotConditionContext> {
    private final DefaultAutomationResolver resolver;

    @Override
    public boolean isSatisfied(Event event, NotConditionContext context) {
        if (ObjectUtils.isEmpty(context.getConditions())) return true;
        return resolver.noneConditionSatisfied(event, context.getConditions());
    }
}