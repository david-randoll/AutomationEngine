package com.davidrandoll.automation.engine.spring.modules.conditions.and;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;

@RequiredArgsConstructor
public class AndCondition extends PluggableCondition<AndConditionContext> {

    @Override
    public boolean isSatisfied(EventContext ec, AndConditionContext cc) {
        if (ObjectUtils.isEmpty(cc.getConditions())) return true;
        return processor.allConditionsSatisfied(ec, cc.getConditions());
    }
}