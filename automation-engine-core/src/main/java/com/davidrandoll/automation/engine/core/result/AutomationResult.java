package com.davidrandoll.automation.engine.core.result;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import lombok.Getter;
import lombok.experimental.Delegate;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Getter
public class AutomationResult {
    private final boolean executed;
    private final boolean paused;
    private final Automation automation;
    private final EventContext context;

    @Delegate
    private final Optional<Object> result;

    private final Map<String, Object> additionalFields;

    private AutomationResult(boolean executed, boolean paused, Automation automation, EventContext context, Object result, Map<String, Object> additionalFields) {
        this.executed = executed;
        this.paused = paused;
        this.automation = new Automation(automation);
        this.context = context;
        this.result = Optional.ofNullable(result);
        this.additionalFields = additionalFields != null ? Map.copyOf(additionalFields) : Collections.emptyMap();
    }

    public static AutomationResult executed(Automation automation, EventContext context, Object result) {
        return new AutomationResult(true, false, automation, context, result, null);
    }

    public static AutomationResult paused(Automation automation, EventContext context, Object result) {
        return new AutomationResult(true, true, automation, context, result, null);
    }

    public static AutomationResult skipped(Automation automation, EventContext context) {
        return new AutomationResult(false, false, automation, context, null, null);
    }

    /**
     * Create an executed AutomationResult with additional fields.
     * Used by interceptors (e.g., tracing) to attach metadata.
     *
     * @param automation       The automation that was executed
     * @param context          The event context
     * @param result           The execution result
     * @param executed         Whether the automation was executed or skipped
     * @param additionalFields Additional metadata fields
     * @return A new AutomationResult with additional fields
     */
    public static AutomationResult executedWithAdditionalFields(Automation automation, EventContext context,
                                                                Object result, boolean executed, boolean paused,
                                                                Map<String, Object> additionalFields) {
        return new AutomationResult(executed, paused, automation, context, result, additionalFields);
    }
}