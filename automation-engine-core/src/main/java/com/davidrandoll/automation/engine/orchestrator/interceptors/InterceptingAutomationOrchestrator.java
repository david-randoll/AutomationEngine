package com.davidrandoll.automation.engine.orchestrator.interceptors;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import com.davidrandoll.automation.engine.orchestrator.IAEOrchestrator;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
public class InterceptingAutomationOrchestrator implements IAEOrchestrator {
    private final IAEOrchestrator delegate;
    private final List<IAutomationExecutionInterceptor> executionInterceptors;
    private final List<IAutomationHandleEventInterceptor> handleEventInterceptors;

    @Override
    public List<Automation> getAutomations() {
        return delegate.getAutomations();
    }

    @Override
    public void registerAutomation(Automation automation) {
        delegate.registerAutomation(automation);
    }

    @Override
    public void removeAutomation(Automation automation) {
        delegate.removeAutomation(automation);
    }

    @Override
    public void removeAllAutomations() {
        delegate.removeAllAutomations();
    }

    @Override
    public void handleEventContext(EventContext eventContext) {
        IAutomationHandleEventChain chain = buildHandleEventChain(0);
        chain.proceed(eventContext);
    }

    @Override
    public void handleEvent(IEvent event) {
        IAutomationHandleEventChain chain = buildHandleEventChain(0);
        chain.proceed(EventContext.of(event));
    }

    @Override
    public void handleEvent(EventContext eventContext, BiConsumer<Automation, EventContext> executionFunction) {
        delegate.handleEvent(eventContext, executionFunction);
    }

    @Override
    public AutomationResult executeAutomation(Automation automation, EventContext context) {
        IAutomationExecutionChain chain = buildExecutionChain(0);
        return chain.proceed(automation, context);
    }

    private IAutomationExecutionChain buildExecutionChain(int index) {
        if (index >= executionInterceptors.size()) {
            return delegate::executeAutomation;
        }

        IAutomationExecutionInterceptor interceptor = executionInterceptors.get(index);
        IAutomationExecutionChain next = buildExecutionChain(index + 1);
        return (automation, context) -> interceptor.intercept(automation, context, next);
    }

    private IAutomationHandleEventChain buildHandleEventChain(int index) {
        if (index >= handleEventInterceptors.size()) {
            return eventContext -> delegate.handleEvent(eventContext, this::executeAutomation);
        }

        IAutomationHandleEventInterceptor interceptor = handleEventInterceptors.get(index);
        IAutomationHandleEventChain next = buildHandleEventChain(index + 1);
        return eventContext -> interceptor.intercept(eventContext, next);
    }
}