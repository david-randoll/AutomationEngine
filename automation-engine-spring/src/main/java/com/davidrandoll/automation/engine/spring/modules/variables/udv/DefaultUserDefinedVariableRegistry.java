package com.davidrandoll.automation.engine.spring.modules.variables.udv;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of IUserDefinedVariableRegistry that stores variables
 * in memory.
 * Users can extend this class or provide their own implementation to load from
 * a database, file, etc.
 */
@Slf4j
public class DefaultUserDefinedVariableRegistry implements IUserDefinedVariableRegistry {
    private final Map<String, UserDefinedVariableDefinition> variables = new ConcurrentHashMap<>();

    @Override
    public Optional<UserDefinedVariableDefinition> findVariable(String name) {
        return Optional.ofNullable(variables.get(name));
    }

    /**
     * Register a user-defined variable.
     *
     * @param definition the variable definition to register
     */
    @Override
    public void registerVariable(UserDefinedVariableDefinition definition) {
        if (definition == null || definition.getName() == null) {
            log.warn("Cannot register variable with null name");
            return;
        }
        variables.put(definition.getName(), definition);
        log.info("Registered user-defined variable: {}", definition.getName());
    }

    /**
     * Unregister a user-defined variable.
     *
     * @param name the name of the variable to unregister
     */
    @Override
    public void unregisterVariable(String name) {
        variables.remove(name);
        log.info("Unregistered user-defined variable: {}", name);
    }

    /**
     * Get all registered variable names.
     *
     * @return a map of all registered variables
     */
    @Override
    public Map<String, UserDefinedVariableDefinition> getAllVariables() {
        return Map.copyOf(variables);
    }
}
