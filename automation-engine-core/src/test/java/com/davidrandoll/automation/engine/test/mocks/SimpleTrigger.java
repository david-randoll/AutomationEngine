package com.davidrandoll.automation.engine.test.mocks;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.triggers.IBaseTrigger;
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
public class SimpleTrigger implements IBaseTrigger {
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
}