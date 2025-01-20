package com.automation.engine.engine.triggers.interceptors;

import com.automation.engine.engine.events.Event;
import com.automation.engine.engine.triggers.IBaseTrigger;
import com.automation.engine.engine.triggers.ITrigger;
import com.automation.engine.engine.triggers.TriggerContext;
import org.springframework.core.OrderComparator;
import org.springframework.util.ObjectUtils;

import java.util.List;

public class InterceptingTrigger implements ITrigger {
    private final IBaseTrigger delegate;
    private final List<ITriggerInterceptor> interceptors;

    public InterceptingTrigger(IBaseTrigger delegate, List<ITriggerInterceptor> interceptors) {
        this.delegate = delegate;
        this.interceptors = ObjectUtils.isEmpty(interceptors) ? List.of() : interceptors.stream()
                .sorted(OrderComparator.INSTANCE)
                .toList();
    }

    @Override
    public boolean isTriggered(Event event, TriggerContext triggerContext) {
        return executeInterceptors(0, event, triggerContext);
    }

    private boolean executeInterceptors(int index, Event event, TriggerContext triggerContext) {
        if (index < interceptors.size()) {
            ITriggerInterceptor interceptor = interceptors.get(index);
            ITrigger action = (e, tc) -> this.executeInterceptors(index + 1, e, tc);
            interceptor.intercept(event, triggerContext, action);
            return true; // doesn't matter what the interceptor returns, the delegate will return the final result
        } else {
            // All interceptors have been processed, execute the delegate
            return delegate.isTriggered(event, triggerContext);
        }
    }
}