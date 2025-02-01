package com.automation.engine.core.conditions;

import com.automation.engine.core.events.Event;

import java.util.ArrayList;

public class BaseConditionList extends ArrayList<IBaseCondition> {
    public boolean allSatisfied(Event context) {
        return this.stream()
                .allMatch(condition -> condition.isSatisfied(context));
    }

    public boolean anySatisfied(Event context) {
        return this.stream()
                .anyMatch(condition -> condition.isSatisfied(context));
    }

    public boolean noneSatisfied(Event context) {
        return this.stream()
                .noneMatch(condition -> condition.isSatisfied(context));
    }
}