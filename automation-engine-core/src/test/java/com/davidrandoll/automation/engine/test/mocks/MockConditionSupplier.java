package com.davidrandoll.automation.engine.test.mocks;

import com.davidrandoll.automation.engine.core.conditions.ICondition;
import com.davidrandoll.automation.engine.creator.conditions.IConditionSupplier;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock implementation of IConditionSupplier for testing.
 */
@Getter
public class MockConditionSupplier implements IConditionSupplier {
    private final Map<String, ICondition> conditions = new HashMap<>();

    public void register(String name, ICondition condition) {
        conditions.put(name, condition);
    }

    @Override
    public ICondition getCondition(String conditionName) {
        return conditions.get(conditionName);
    }
}
