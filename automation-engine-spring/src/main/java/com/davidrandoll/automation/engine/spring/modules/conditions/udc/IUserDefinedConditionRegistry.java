package com.davidrandoll.automation.engine.spring.modules.conditions.udc;

import java.util.Optional;

/**
 * Registry interface for finding user-defined conditions.
 * Implementations should provide the logic to look up condition definitions by name.
 */
public interface IUserDefinedConditionRegistry {

    /**
     * Find a user-defined condition by name.
     *
     * @param name the name of the condition to find
     * @return an Optional containing the condition definition if found, empty otherwise
     */
    Optional<UserDefinedConditionDefinition> findCondition(String name);
}

