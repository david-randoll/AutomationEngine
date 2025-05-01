package com.automation.engine.modules.conditions.or;


import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("orCondition")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "orCondition", ignored = OrCondition.class)
public class OrCondition extends PluggableCondition<OrConditionContext> {

    @Override
    public boolean isSatisfied(EventContext ec, OrConditionContext cc) {
        if (ObjectUtils.isEmpty(cc.getConditions())) return true;
        return processor.anyConditionSatisfied(ec, cc.getConditions());
    }
}