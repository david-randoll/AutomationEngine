package com.davidrandoll.automation.engine.core.triggers.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.triggers.ITrigger;
import com.davidrandoll.automation.engine.core.triggers.TriggerContext;

import java.util.List;
import java.util.Optional;

public class InterceptingTrigger implements ITrigger {
    private final ITrigger delegate;
    private final List<ITriggerInterceptor> interceptors;

    public InterceptingTrigger(ITrigger delegate, List<ITriggerInterceptor> interceptors) {
        this.delegate = delegate;
        this.interceptors = Optional.ofNullable(interceptors).orElse(List.of());
    }

    @Override
    public boolean isTriggered(EventContext eventContext, TriggerContext triggerContext) {
        ITrigger chain = buildChain(0);
        return chain.isTriggered(eventContext, triggerContext);
    }

    private ITrigger buildChain(int index) {
        if (index >= interceptors.size()) {
            return delegate;
        }

        ITriggerInterceptor interceptor = interceptors.get(index);
        ITrigger next = buildChain(index + 1);
        return (ec, tc) -> interceptor.intercept(ec, tc, next);
    }
}