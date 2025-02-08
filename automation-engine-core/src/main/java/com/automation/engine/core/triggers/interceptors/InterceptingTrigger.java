package com.automation.engine.core.triggers.interceptors;

import com.automation.engine.core.events.Event;
import com.automation.engine.core.triggers.IBaseTrigger;
import com.automation.engine.core.triggers.ITrigger;
import com.automation.engine.core.triggers.TriggerContext;
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

    private boolean executeInterceptors(int index, Event event, TriggerContext context) {
        if (index < interceptors.size()) {
            ITriggerInterceptor interceptor = interceptors.get(index);
            final boolean[] resultHolder = {false};
            ITrigger trigger = (ec, cc) -> resultHolder[0] = this.executeInterceptors(index + 1, ec, cc);
            interceptor.intercept(event, context, trigger);
            return resultHolder[0];
        } else {
            // All interceptors processed, execute the delegate
            return delegate.isTriggered(event, context);
        }
    }
}