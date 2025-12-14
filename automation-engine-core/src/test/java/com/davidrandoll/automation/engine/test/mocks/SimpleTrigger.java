package com.davidrandoll.automation.engine.test.mocks;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.triggers.IBaseTrigger;
import com.davidrandoll.automation.engine.core.triggers.ITrigger;
import com.davidrandoll.automation.engine.core.triggers.TriggerContext;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple trigger implementation for testing.
 * Allows setting the activation result.
 */
@Getter
@Setter
public class SimpleTrigger implements IBaseTrigger, ITrigger {
    private final String name;
    private boolean activated = true;
    private int checkCount = 0;
    private final List<EventContext> checkedContexts = new ArrayList<>();

    public SimpleTrigger(String name) {
        this.name = name;
    }

    public SimpleTrigger(String name, boolean activated) {
        this.name = name;
        this.activated = activated;
    }

    public void reset() {
        checkCount = 0;
        checkedContexts.clear();
        activated = true;
    }

    @Override
    public boolean isTriggered(EventContext eventContext) {
        checkCount++;
        checkedContexts.add(eventContext);
        return activated;
    }

    @Override
    public boolean isTriggered(EventContext context, TriggerContext triggerContext) {
        return isTriggered(context);
    }

    @Override
    public Class<?> getContextType() {
        return TriggerContext.class;
    }
}