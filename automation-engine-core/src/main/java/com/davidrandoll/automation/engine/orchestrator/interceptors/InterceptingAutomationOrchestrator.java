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
    private final List<IAutomationExecutionInterceptor> interceptors;

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
        this.handleEvent(eventContext, this::executeAutomation);
    }

    @Override
    public void handleEvent(IEvent event) {
        this.handleEvent(EventContext.of(event), this::executeAutomation);
    }

    @Override
    public void handleEvent(EventContext eventContext, BiConsumer<Automation, EventContext> executionFunction) {
        delegate.handleEvent(eventContext, executionFunction);
    }

    @Override
    public AutomationResult executeAutomation(Automation automation, EventContext context) {
        IAutomationExecutionChain chain = buildChain(0);
        return chain.proceed(automation, context);
    }

    private IAutomationExecutionChain buildChain(int index) {
        if (index >= interceptors.size()) {
            return delegate::executeAutomation;
        }

        IAutomationExecutionInterceptor currentFilter = interceptors.get(index);
        IAutomationExecutionChain next = buildChain(index + 1);
        return (automation, context) -> currentFilter.intercept(automation, context, next);
    }
}