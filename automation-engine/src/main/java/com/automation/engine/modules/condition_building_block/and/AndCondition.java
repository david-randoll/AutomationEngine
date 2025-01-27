package com.automation.engine.modules.condition_building_block.and;

import com.automation.engine.core.conditions.AbstractCondition;
import com.automation.engine.core.conditions.IBaseCondition;
import com.automation.engine.core.events.Event;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component("andCondition")
@RequiredArgsConstructor
public class AndCondition extends AbstractCondition<AndConditionContext> {
    private final DefaultAutomationResolver resolver;

    @Override
    public boolean isSatisfied(Event context, AndConditionContext conditionContext) {
        if (ObjectUtils.isEmpty(conditionContext.getConditions())) return true;
        List<IBaseCondition> conditions = resolver.buildConditionsList(conditionContext.getConditions());
        return conditions.stream()
                .allMatch(condition -> condition.isSatisfied(context));
    }
}