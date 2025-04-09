package com.automation.engine.creator.triggers;

import com.automation.engine.core.triggers.ITrigger;

public interface ITriggerSupplier {
    ITrigger getTrigger(String name);
}