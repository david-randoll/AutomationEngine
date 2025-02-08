package com.automation.engine.modules.condition_building_block.and;

import com.automation.engine.core.conditions.AbstractCondition;
import com.automation.engine.core.events.Event;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("andCondition")
@RequiredArgsConstructor
public class AndCondition extends AbstractCondition<AndConditionContext> {
    private final DefaultAutomationResolver resolver;

    @Override
    public boolean isSatisfied(Event event, AndConditionContext context) {
        if (ObjectUtils.isEmpty(context.getConditions())) return true;
        return resolver.allConditionsSatisfied(event, context.getConditions());
    }
}