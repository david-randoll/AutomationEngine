package com.automation.engine.modules.condition_building_block.or;

import com.automation.engine.core.conditions.AbstractCondition;
import com.automation.engine.core.events.Event;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("orCondition")
@RequiredArgsConstructor
public class OrCondition extends AbstractCondition<OrConditionContext> {
    private final DefaultAutomationResolver resolver;

    @Override
    public boolean isSatisfied(Event context, OrConditionContext conditionContext) {
        if (ObjectUtils.isEmpty(conditionContext.getConditions())) return true;
        return resolver.anyConditionSatisfied(context, conditionContext.getConditions());
    }
}