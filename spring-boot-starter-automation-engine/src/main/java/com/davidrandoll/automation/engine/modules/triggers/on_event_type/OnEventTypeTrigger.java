package com.davidrandoll.automation.engine.modules.triggers.on_event_type;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableTrigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OnEventTypeTrigger extends PluggableTrigger<OnEventTypeTriggerContext> {

    @Override
    public boolean isTriggered(EventContext ec, OnEventTypeTriggerContext tc) {
        var event = ec.getEvent();

        if (event == null) {
            log.debug("No event present in context");
            return false;
        }

        String actualEventType = event.getEventType();
        String simpleName = getSimpleClassName(actualEventType);

        // Exact match (full class name)
        if (tc.getEventType() != null && tc.getEventType().equalsIgnoreCase(actualEventType)) {
            log.debug("Matched eventType: {}", actualEventType);
            return true;
        }

        // Match on simple name
        if (tc.getEventName() != null && tc.getEventName().equalsIgnoreCase(simpleName)) {
            log.debug("Matched eventName (simple class name): {}", simpleName);
            return true;
        }

        // Match using regex
        if (tc.getRegex() != null) {
            if (actualEventType.matches(tc.getRegex()) || simpleName.matches(tc.getRegex())) {
                log.debug("Matched eventType or eventName via regex: {}", tc.getRegex());
                return true;
            }
        }

        return false;
    }

    private String getSimpleClassName(String fqcn) {
        if (fqcn == null) return "";
        int lastDot = fqcn.lastIndexOf('.');
        return (lastDot >= 0) ? fqcn.substring(lastDot + 1) : fqcn;
    }
}
