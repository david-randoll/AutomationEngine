package com.davidrandoll.automation.engine.templating.interceptors;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import com.davidrandoll.automation.engine.orchestrator.interceptors.IAutomationExecutionChain;
import com.davidrandoll.automation.engine.orchestrator.interceptors.IAutomationExecutionInterceptor;

public class AutomationOptionsInterceptor implements IAutomationExecutionInterceptor {
    public static final String AUTOMATION_OPTIONS_KEY = "automationOptions";

    @Override
    public AutomationResult intercept(Automation automation, EventContext context, IAutomationExecutionChain chain) {
        context.addMetadata(AUTOMATION_OPTIONS_KEY, automation.getOptions());
        try {
            return chain.proceed(automation, context);
        } finally {
            context.removeMetadata(AUTOMATION_OPTIONS_KEY);
        }
    }
}