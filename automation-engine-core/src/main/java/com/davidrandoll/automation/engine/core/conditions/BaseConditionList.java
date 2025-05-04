package com.davidrandoll.automation.engine.core.conditions;

import com.davidrandoll.automation.engine.core.events.EventContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BaseConditionList extends ArrayList<IBaseCondition> {

    public boolean allSatisfied(EventContext context) {
        return this.stream()
                .allMatch(condition -> condition.isSatisfied(context));
    }

    public boolean anySatisfied(EventContext context) {
        return this.stream()
                .anyMatch(condition -> condition.isSatisfied(context));
    }

    public boolean noneSatisfied(EventContext context) {
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