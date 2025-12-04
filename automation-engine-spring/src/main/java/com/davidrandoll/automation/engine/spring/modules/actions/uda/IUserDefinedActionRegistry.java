package com.davidrandoll.automation.engine.spring.modules.actions.uda;

import java.util.Map;
import java.util.Optional;

/**
 * Registry interface for managing user-defined actions.
 * Implementations should provide the logic to register, unregister, and look up
 * action definitions.
 */
public interface IUserDefinedActionRegistry {

    /**
     * Find a user-defined action by name.
     *
     * @param name the name of the action to find
     * @return an Optional containing the action definition if found, empty
     *         otherwise
     */
    Optional<UserDefinedActionDefinition> findAction(String name);

    /**
     * Register a user-defined action.
     *
     * @param definition the action definition to register
     */
    void registerAction(UserDefinedActionDefinition definition);

    /**
     * Unregister a user-defined action.
     *
     * @param name the name of the action to unregister
     */
    void unregisterAction(String name);

    /**
     * Get all registered actions.
     *
     * @return a map of all registered actions (name -> definition)
     */
    Map<String, UserDefinedActionDefinition> getAllActions();
}
