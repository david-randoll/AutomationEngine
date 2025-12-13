package com.davidrandoll.automation.engine.test.mocks;

import com.davidrandoll.automation.engine.core.triggers.ITrigger;
import com.davidrandoll.automation.engine.creator.triggers.ITriggerSupplier;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock implementation of ITriggerSupplier for testing.
 */
@Getter
public class MockTriggerSupplier implements ITriggerSupplier {
    private final Map<String, ITrigger> triggers = new HashMap<>();

    public void register(String name, ITrigger trigger) {
        triggers.put(name, trigger);
    }

    @Override
    public ITrigger getTrigger(String triggerName) {
        return triggers.get(triggerName);
    }
}
