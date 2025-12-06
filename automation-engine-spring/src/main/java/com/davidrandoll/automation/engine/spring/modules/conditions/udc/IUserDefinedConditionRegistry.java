package com.davidrandoll.automation.engine.spring.modules.conditions.udc;

import java.util.Map;
import java.util.Optional;

/**
 * Registry interface for managing user-defined conditions.
 * Implementations should provide the logic to register, unregister, and look up
 * condition definitions.
 */
public interface IUserDefinedConditionRegistry {

    /**
     * Find a user-defined condition by name.
     *
     * @param name the name of the condition to find
     * @return an Optional containing the condition definition if found, empty
     *         otherwise
     */
    Optional<UserDefinedConditionDefinition> findCondition(String name);

    /**
     * Register a user-defined condition.
     *
     * @param definition the condition definition to register
     */
    void registerCondition(UserDefinedConditionDefinition definition);

    /**
     * Unregister a user-defined condition.
     *
     * @param name the name of the condition to unregister
     */
    void unregisterCondition(String name);

    /**
     * Get all registered conditions.
     *
     * @return a map of all registered conditions (name -> definition)
     */
    Map<String, UserDefinedConditionDefinition> getAllConditions();
}
