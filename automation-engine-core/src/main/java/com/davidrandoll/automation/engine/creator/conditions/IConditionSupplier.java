package com.davidrandoll.automation.engine.creator.conditions;

import com.davidrandoll.automation.engine.core.conditions.ICondition;

public interface IConditionSupplier {
    ICondition getCondition(String name);
}