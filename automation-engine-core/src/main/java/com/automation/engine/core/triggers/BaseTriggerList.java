package com.automation.engine.core.triggers;

import com.automation.engine.core.events.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BaseTriggerList extends ArrayList<IBaseTrigger> {
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

    public static BaseTriggerList of(IBaseTrigger... triggers) {
        BaseTriggerList triggerList = new BaseTriggerList();
        Collections.addAll(triggerList, triggers);
        return triggerList;
    }

    public static BaseTriggerList of(List<IBaseTrigger> triggers) {
        BaseTriggerList triggerList = new BaseTriggerList();
        triggerList.addAll(triggers);
        return triggerList;
    }
}