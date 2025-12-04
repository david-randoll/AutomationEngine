package com.davidrandoll.automation.engine.spring.modules.actions.uda;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of IUserDefinedActionRegistry that stores actions in
 * memory.
 * Users can extend this class or provide their own implementation to load from
 * a database, file, etc.
 */
@Slf4j
public class DefaultUserDefinedActionRegistry implements IUserDefinedActionRegistry {
    private final Map<String, UserDefinedActionDefinition> actions = new ConcurrentHashMap<>();

    @Override
    public Optional<UserDefinedActionDefinition> findAction(String name) {
        return Optional.ofNullable(actions.get(name));
    }

    /**
     * Register a user-defined action.
     *
     * @param definition the action definition to register
     */
    @Override
    public void registerAction(UserDefinedActionDefinition definition) {
        if (definition == null || definition.getName() == null) {
            log.warn("Cannot register action with null name");
            return;
        }
        actions.put(definition.getName(), definition);
        log.info("Registered user-defined action: {}", definition.getName());
    }

    /**
     * Unregister a user-defined action.
     *
     * @param name the name of the action to unregister
     */
    @Override
    public void unregisterAction(String name) {
        actions.remove(name);
        log.info("Unregistered user-defined action: {}", name);
    }

    /**
     * Get all registered action names.
     *
     * @return a set of all registered action names
     */
    @Override
    public Map<String, UserDefinedActionDefinition> getAllActions() {
        return Map.copyOf(actions);
    }
}
