package com.automation.engine.core.triggers.interceptors;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.core.triggers.IBaseTrigger;
import com.automation.engine.core.triggers.ITrigger;
import com.automation.engine.core.triggers.TriggerContext;

import java.util.List;
import java.util.Optional;

public class InterceptingTrigger implements ITrigger {
    private final IBaseTrigger delegate;
    private final List<ITriggerInterceptor> interceptors;

    public InterceptingTrigger(IBaseTrigger delegate, List<ITriggerInterceptor> interceptors) {
        this.delegate = delegate;
        this.interceptors = Optional.ofNullable(interceptors).orElse(List.of());
    }

    @Override
    public boolean isTriggered(EventContext eventContext, TriggerContext triggerContext) {
        return executeInterceptors(0, eventContext, triggerContext);
    }

    private boolean executeInterceptors(int index, EventContext eventContext, TriggerContext context) {
        if (index < interceptors.size()) {
            ITriggerInterceptor interceptor = interceptors.get(index);
            final boolean[] resultHolder = {false};
            ITrigger trigger = (ec, cc) -> resultHolder[0] = this.executeInterceptors(index + 1, ec, cc);
            interceptor.intercept(eventContext, context, trigger);
            return resultHolder[0];
        } else {
            // All interceptors processed, execute the delegate
            return delegate.isTriggered(eventContext, context);
        }
    }
}