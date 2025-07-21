package com.davidrandoll.automation.engine.modules.conditions.not;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("notCondition")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "notCondition", ignored = NotCondition.class)
public class NotCondition extends PluggableCondition<NotConditionContext> {

    @Override
    public boolean isSatisfied(EventContext ec, NotConditionContext cc) {
        if (ObjectUtils.isEmpty(cc.getConditions())) return true;
        return processor.noneConditionSatisfied(ec, cc.getConditions());
    }
}