package com.davidrandoll.automation.engine.creator.triggers;

import com.davidrandoll.automation.engine.core.triggers.ITrigger;

public interface ITriggerSupplier {
    ITrigger getTrigger(String name);
}