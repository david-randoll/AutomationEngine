package com.davidrandoll.automation.engine.spring.modules.actions.uda;

import java.util.Optional;

/**
 * Registry interface for finding user-defined actions.
 * Implementations should provide the logic to look up action definitions by name.
 */
public interface IUserDefinedActionRegistry {

    /**
     * Find a user-defined action by name.
     *
     * @param name the name of the action to find
     * @return an Optional containing the action definition if found, empty otherwise
     */
    Optional<UserDefinedActionDefinition> findAction(String name);
}

