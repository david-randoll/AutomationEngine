package com.automation.engine.core.conditions;

import com.automation.engine.core.events.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public static BaseConditionList of(IBaseCondition... conditions) {
        BaseConditionList list = new BaseConditionList();
        Collections.addAll(list, conditions);
        return list;
    }

    public static BaseConditionList of(List<IBaseCondition> conditions) {
        BaseConditionList list = new BaseConditionList();
        list.addAll(conditions);
        return list;
    }
}