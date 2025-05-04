package com.davidrandoll.automation.engine.modules.conditions.always_false;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component("alwaysFalseCondition")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "alwaysFalseCondition", ignored = AlwaysFalseCondition.class)
public class AlwaysFalseCondition extends PluggableCondition<AlwaysFalseConditionContext> {
    @Override
    public boolean isSatisfied(EventContext ec, AlwaysFalseConditionContext cc) {
        return false;
    }
}