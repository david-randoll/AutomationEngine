package com.automation.engine.modules.conditions.always_false;

import com.automation.engine.conditional.AEConditionalOnMissingBeanName;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("alwaysFalseCondition")
@RequiredArgsConstructor
@AEConditionalOnMissingBeanName
public class AlwaysFalseCondition extends PluggableCondition<AlwaysFalseConditionContext> {
    @Override
    public boolean isSatisfied(EventContext ec, AlwaysFalseConditionContext cc) {
        return false;
    }
}