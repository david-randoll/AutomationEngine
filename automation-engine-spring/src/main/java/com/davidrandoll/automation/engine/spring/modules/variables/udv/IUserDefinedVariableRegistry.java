package com.davidrandoll.automation.engine.spring.modules.variables.udv;

import java.util.Map;
import java.util.Optional;

/**
 * Registry interface for managing user-defined variables.
 * Implementations should provide the logic to register, unregister, and look up
 * variable definitions.
 */
public interface IUserDefinedVariableRegistry {

    /**
     * Find a user-defined variable by name.
     *
     * @param name the name of the variable to find
     * @return an Optional containing the variable definition if found, empty
     *         otherwise
     */
    Optional<UserDefinedVariableDefinition> findVariable(String name);

    /**
     * Register a user-defined variable.
     *
     * @param definition the variable definition to register
     */
    void registerVariable(UserDefinedVariableDefinition definition);

    /**
     * Unregister a user-defined variable.
     *
     * @param name the name of the variable to unregister
     */
    void unregisterVariable(String name);

    /**
     * Get all registered variables.
     *
     * @return a map of all registered variables (name -> definition)
     */
    Map<String, UserDefinedVariableDefinition> getAllVariables();
}
