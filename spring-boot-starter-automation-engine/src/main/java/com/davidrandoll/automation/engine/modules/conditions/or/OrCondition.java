package com.davidrandoll.automation.engine.modules.conditions.or;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;

@RequiredArgsConstructor
public class OrCondition extends PluggableCondition<OrConditionContext> {

    @Override
    public boolean isSatisfied(EventContext ec, OrConditionContext cc) {
        if (ObjectUtils.isEmpty(cc.getConditions())) return true;
        return processor.anyConditionSatisfied(ec, cc.getConditions());
    }
}