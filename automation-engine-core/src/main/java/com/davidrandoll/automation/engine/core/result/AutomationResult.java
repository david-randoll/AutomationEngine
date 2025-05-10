package com.davidrandoll.automation.engine.core.result;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import lombok.Getter;
import lombok.experimental.Delegate;

import java.util.Optional;

@Getter
public class AutomationResult {
    private final boolean executed;
    private final Automation automation;
    private final EventContext context;

    @Delegate
    private final Optional<Object> result;

    private AutomationResult(boolean executed, Automation automation, EventContext context, Object result) {
        this.executed = executed;
        this.automation = new Automation(automation);
        this.context = context;
        this.result = Optional.ofNullable(result);
    }

    public static AutomationResult executed(Automation automation, EventContext context, Object result) {
        return new AutomationResult(true, automation, context, result);
    }

    public static AutomationResult skipped(Automation automation, EventContext context) {
        return new AutomationResult(false, automation, context, null);
    }
}