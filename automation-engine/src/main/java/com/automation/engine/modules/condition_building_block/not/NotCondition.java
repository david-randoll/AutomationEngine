package com.automation.engine.modules.condition_building_block.not;

import com.automation.engine.core.conditions.AbstractCondition;
import com.automation.engine.core.conditions.IBaseCondition;
import com.automation.engine.core.events.Event;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component("notCondition")
@RequiredArgsConstructor
public class NotCondition extends AbstractCondition<NotConditionContext> {
    private final DefaultAutomationResolver resolver;

    @Override
    public boolean isSatisfied(Event context, NotConditionContext conditionContext) {
        if (ObjectUtils.isEmpty(conditionContext.getConditions())) return true;
        List<IBaseCondition> conditions = resolver.buildConditionsList(conditionContext.getConditions());
        return conditions.stream()
                .noneMatch(condition -> condition.isSatisfied(context));
    }
}