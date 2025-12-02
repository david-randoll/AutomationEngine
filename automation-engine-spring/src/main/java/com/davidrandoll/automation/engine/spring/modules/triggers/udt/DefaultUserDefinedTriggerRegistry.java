package com.davidrandoll.automation.engine.spring.modules.triggers.udt;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of IUserDefinedTriggerRegistry that stores triggers in memory.
 * Users can extend this class or provide their own implementation to load from a database, file, etc.
 */
@Slf4j
public class DefaultUserDefinedTriggerRegistry implements IUserDefinedTriggerRegistry {
    private final Map<String, UserDefinedTriggerDefinition> triggers = new ConcurrentHashMap<>();

    @Override
    public Optional<UserDefinedTriggerDefinition> findTrigger(String name) {
        return Optional.ofNullable(triggers.get(name));
    }

    /**
     * Register a user-defined trigger.
     *
     * @param definition the trigger definition to register
     */
    public void registerTrigger(UserDefinedTriggerDefinition definition) {
        if (definition == null || definition.getName() == null) {
            log.warn("Cannot register trigger with null name");
            return;
        }
        triggers.put(definition.getName(), definition);
        log.info("Registered user-defined trigger: {}", definition.getName());
    }

    /**
     * Unregister a user-defined trigger.
     *
     * @param name the name of the trigger to unregister
     */
    public void unregisterTrigger(String name) {
        triggers.remove(name);
        log.info("Unregistered user-defined trigger: {}", name);
    }

    /**
     * Get all registered trigger names.
     *
     * @return a map of all registered triggers
     */
    public Map<String, UserDefinedTriggerDefinition> getAllTriggers() {
        return Map.copyOf(triggers);
    }
}

