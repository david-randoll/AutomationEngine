package com.automation.engine.core.triggers;

import com.automation.engine.core.events.EventContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BaseTriggerList extends ArrayList<IBaseTrigger> {
    public boolean allTriggered(EventContext eventContext) {
        return this.stream()
                .allMatch(trigger -> trigger.isTriggered(eventContext));
    }

    public boolean anyTriggered(EventContext eventContext) {
        return this.stream()
                .anyMatch(trigger -> trigger.isTriggered(eventContext));
    }

    public boolean noneTriggered(EventContext eventContext) {
        return this.stream()
                .noneMatch(trigger -> trigger.isTriggered(eventContext));
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