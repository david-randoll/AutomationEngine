package com.davidrandoll.automation.engine.spring.tx.interceptors;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import com.davidrandoll.automation.engine.orchestrator.interceptors.IAutomationExecutionChain;
import com.davidrandoll.automation.engine.orchestrator.interceptors.IAutomationExecutionInterceptor;
import com.davidrandoll.automation.engine.spring.tx.TransactionalRunner;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransactionalExecutionInterceptor implements IAutomationExecutionInterceptor {
    private final TransactionalRunner transactionalRunner;

    @Override
    public AutomationResult intercept(Automation automation, EventContext context, IAutomationExecutionChain chain) {
        if (isTransactional(automation)) {
            return transactionalRunner.runInTransaction(() -> chain.proceed(automation, context));
        }

        return chain.proceed(automation, context);
    }

    private static boolean isTransactional(Automation automation) {
        Object transactional = automation.getOptions().get("transactional");
        if (transactional instanceof Boolean bool) return bool;
        if (transactional instanceof String str) {
            return "true".equalsIgnoreCase(str) || "yes".equalsIgnoreCase(str) || "1".equals(str);
        }
        if (transactional instanceof Number num) {
            return num.intValue() != 0;
        }
        return false;
    }
}