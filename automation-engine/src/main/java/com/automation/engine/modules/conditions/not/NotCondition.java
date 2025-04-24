package com.automation.engine.modules.conditions.not;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("notCondition")
@RequiredArgsConstructor
public class NotCondition extends PluggableCondition<NotConditionContext> {

    @Override
    public boolean isSatisfied(EventContext ec, NotConditionContext cc) {
        if (ObjectUtils.isEmpty(cc.getConditions())) return true;
        return processor.noneConditionSatisfied(ec, cc.getConditions());
    }
}