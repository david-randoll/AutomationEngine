package com.davidrandoll.automation.engine.spring.modules.triggers.udt;

import java.util.Optional;

/**
 * Registry interface for finding user-defined triggers.
 * Implementations should provide the logic to look up trigger definitions by name.
 */
public interface IUserDefinedTriggerRegistry {

    /**
     * Find a user-defined trigger by name.
     *
     * @param name the name of the trigger to find
     * @return an Optional containing the trigger definition if found, empty otherwise
     */
    Optional<UserDefinedTriggerDefinition> findTrigger(String name);
}

