package com.davidrandoll.automation.engine.core.conditions;

import com.davidrandoll.automation.engine.core.IBlock;
import com.davidrandoll.automation.engine.core.events.EventContext;

@FunctionalInterface
public interface ICondition extends IBlock {
    boolean isSatisfied(EventContext context, ConditionContext conditionContext);
}