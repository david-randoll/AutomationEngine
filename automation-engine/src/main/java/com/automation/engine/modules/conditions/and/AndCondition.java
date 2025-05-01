package com.automation.engine.modules.conditions.and;


import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("andCondition")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "andCondition", ignored = AndCondition.class)
public class AndCondition extends PluggableCondition<AndConditionContext> {

    @Override
    public boolean isSatisfied(EventContext ec, AndConditionContext cc) {
        if (ObjectUtils.isEmpty(cc.getConditions())) return true;
        return processor.allConditionsSatisfied(ec, cc.getConditions());
    }
}