package com.automation.engine.core.triggers;

import com.automation.engine.core.events.Event;

import java.util.ArrayList;

public class TriggerList extends ArrayList<IBaseTrigger> {
    public boolean allTriggered(Event event) {
        return this.stream()
                .allMatch(trigger -> trigger.isTriggered(event));
    }

    public boolean anyTriggered(Event event) {
        return this.stream()
                .anyMatch(trigger -> trigger.isTriggered(event));
    }

    public boolean noneTriggered(Event event) {
        return this.stream()
                .noneMatch(trigger -> trigger.isTriggered(event));
    }
}