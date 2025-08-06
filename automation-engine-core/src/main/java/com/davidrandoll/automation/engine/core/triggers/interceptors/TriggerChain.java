package com.davidrandoll.automation.engine.core.triggers.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.triggers.ITrigger;
import com.davidrandoll.automation.engine.core.triggers.TriggerContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TriggerChain implements ITriggerChain {
    private final ITriggerChain delegate;
    private final ITrigger trigger;

    @Override
    public boolean isTriggered(EventContext eventContext, TriggerContext triggerContext) {
        return delegate.isTriggered(eventContext, triggerContext);
    }

    @Override
    public Class<?> getContextType() {
        return trigger.getContextType();
    }

    @Override
    public boolean autoEvaluateExpression() {
        return trigger.autoEvaluateExpression();
    }
}