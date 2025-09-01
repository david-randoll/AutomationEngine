package com.davidrandoll.automation.engine.spring.modules.conditions.not;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;

@RequiredArgsConstructor
public class NotCondition extends PluggableCondition<NotConditionContext> {

    @Override
    public boolean isSatisfied(EventContext ec, NotConditionContext cc) {
        if (ObjectUtils.isEmpty(cc.getConditions())) return true;
        return processor.noneConditionSatisfied(ec, cc.getConditions());
    }
}