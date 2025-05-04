package com.davidrandoll.automation.engine.modules.conditions.always_true;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component("alwaysTrueCondition")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "alwaysTrueCondition", ignored = AlwaysTrueCondition.class)
public class AlwaysTrueCondition extends PluggableCondition<AlwaysTrueConditionContext> {
    @Override
    public boolean isSatisfied(EventContext ec, AlwaysTrueConditionContext cc) {
        return true;
    }
}
