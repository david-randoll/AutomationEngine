package com.davidrandoll.automation.engine.modules.conditions.on_event_type;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component("onEventTypeCondition")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "onEventTypeCondition", ignored = OnEventTypeCondition.class)
public class OnEventTypeCondition extends PluggableCondition<OnEventTypeConditionContext> {
    @Override
    public boolean isSatisfied(EventContext ec, OnEventTypeConditionContext cc) {
        var event = ec.getEvent();

        if (event == null) {
            log.debug("No event present in context");
            return false;
        }

        String actualEventType = event.getEventType();
        String simpleName = getSimpleClassName(actualEventType);

        // Exact match (full class name)
        if (cc.getEventType() != null && cc.getEventType().equalsIgnoreCase(actualEventType)) {
            log.debug("Matched eventType: {}", actualEventType);
            return true;
        }

        // Match on simple name
        if (cc.getEventName() != null && cc.getEventName().equalsIgnoreCase(simpleName)) {
            log.debug("Matched eventName (simple class name): {}", simpleName);
            return true;
        }

        // Match using regex
        if (cc.getRegex() != null) {
            if (actualEventType.matches(cc.getRegex()) || simpleName.matches(cc.getRegex())) {
                log.debug("Matched eventType or eventName via regex: {}", cc.getRegex());
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
