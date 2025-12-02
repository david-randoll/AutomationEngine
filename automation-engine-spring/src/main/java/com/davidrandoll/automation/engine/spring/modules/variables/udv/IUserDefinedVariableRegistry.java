package com.davidrandoll.automation.engine.spring.modules.variables.udv;

import java.util.Optional;

/**
 * Registry interface for finding user-defined variables.
 * Implementations should provide the logic to look up variable definitions by name.
 */
public interface IUserDefinedVariableRegistry {

    /**
     * Find a user-defined variable by name.
     *
     * @param name the name of the variable to find
     * @return an Optional containing the variable definition if found, empty otherwise
     */
    Optional<UserDefinedVariableDefinition> findVariable(String name);
}

