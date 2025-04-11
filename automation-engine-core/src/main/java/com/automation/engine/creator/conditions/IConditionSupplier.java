package com.automation.engine.creator.conditions;

import com.automation.engine.core.conditions.ICondition;

public interface IConditionSupplier {
    ICondition getCondition(String name);
}