package com.davidrandoll.automation.engine.spring.tx.interceptors;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import com.davidrandoll.automation.engine.orchestrator.interceptors.IAutomationExecutionChain;
import com.davidrandoll.automation.engine.orchestrator.interceptors.IAutomationExecutionInterceptor;
import com.davidrandoll.automation.engine.spring.tx.TransactionalRunner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionalExecutionInterceptor implements IAutomationExecutionInterceptor {

    private final TransactionalRunner transactionalRunner;

    @Override
    public AutomationResult intercept(Automation automation, EventContext context, IAutomationExecutionChain chain) {
        Object transactional = automation.getOptions().get("transactional");

        if (Boolean.TRUE.equals(transactional)) {
            return transactionalRunner.runInTransaction(() -> chain.proceed(automation, context));
        }

        return chain.proceed(automation, context);
    }
}
