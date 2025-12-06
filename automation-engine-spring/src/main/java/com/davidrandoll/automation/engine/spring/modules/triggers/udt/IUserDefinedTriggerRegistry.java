package com.davidrandoll.automation.engine.spring.modules.triggers.udt;

import java.util.Map;
import java.util.Optional;

/**
 * Registry interface for managing user-defined triggers.
 * Implementations should provide the logic to register, unregister, and look up
 * trigger definitions.
 */
public interface IUserDefinedTriggerRegistry {

    /**
     * Find a user-defined trigger by name.
     *
     * @param name the name of the trigger to find
     * @return an Optional containing the trigger definition if found, empty
     *         otherwise
     */
    Optional<UserDefinedTriggerDefinition> findTrigger(String name);

    /**
     * Register a user-defined trigger.
     *
     * @param definition the trigger definition to register
     */
    void registerTrigger(UserDefinedTriggerDefinition definition);

    /**
     * Unregister a user-defined trigger.
     *
     * @param name the name of the trigger to unregister
     */
    void unregisterTrigger(String name);

    /**
     * Get all registered triggers.
     *
     * @return a map of all registered triggers (name -> definition)
     */
    Map<String, UserDefinedTriggerDefinition> getAllTriggers();
}
