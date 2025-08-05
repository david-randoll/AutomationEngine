package com.davidrandoll.automation.engine.creator.triggers;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.triggers.IBaseTrigger;
import com.davidrandoll.automation.engine.core.triggers.ITrigger;
import com.davidrandoll.automation.engine.core.triggers.TriggerContext;
import com.davidrandoll.automation.engine.core.triggers.interceptors.ITriggerInterceptor;
import com.davidrandoll.automation.engine.core.triggers.interceptors.InterceptingTrigger;

import java.util.List;

public class EvaluatableTrigger implements IBaseTrigger {
    private final ITrigger delegate;
    private final TriggerContext triggerContext;
    private final InterceptingTrigger interceptingTrigger;

    public EvaluatableTrigger(ITrigger delegate, TriggerContext triggerContext, List<ITriggerInterceptor> interceptors) {
        this.delegate = delegate;
        this.triggerContext = triggerContext;
        this.interceptingTrigger = new InterceptingTrigger(delegate, interceptors);
    }

    @Override
    public boolean isTriggered(EventContext eventContext) {
        return interceptingTrigger.isTriggered(eventContext, triggerContext);
    }

    public ITrigger getRawTrigger() {
        return delegate;
    }

    public TriggerContext getRawContext() {
        return triggerContext;
    }
}