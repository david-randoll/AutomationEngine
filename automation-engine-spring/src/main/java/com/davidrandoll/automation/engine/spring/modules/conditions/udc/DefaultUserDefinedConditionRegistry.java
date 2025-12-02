package com.davidrandoll.automation.engine.spring.modules.conditions.udc;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of IUserDefinedConditionRegistry that stores conditions in memory.
 * Users can extend this class or provide their own implementation to load from a database, file, etc.
 */
@Slf4j
public class DefaultUserDefinedConditionRegistry implements IUserDefinedConditionRegistry {
    private final Map<String, UserDefinedConditionDefinition> conditions = new ConcurrentHashMap<>();

    @Override
    public Optional<UserDefinedConditionDefinition> findCondition(String name) {
        return Optional.ofNullable(conditions.get(name));
    }

    /**
     * Register a user-defined condition.
     *
     * @param definition the condition definition to register
     */
    public void registerCondition(UserDefinedConditionDefinition definition) {
        if (definition == null || definition.getName() == null) {
            log.warn("Cannot register condition with null name");
            return;
        }
        conditions.put(definition.getName(), definition);
        log.info("Registered user-defined condition: {}", definition.getName());
    }

    /**
     * Unregister a user-defined condition.
     *
     * @param name the name of the condition to unregister
     */
    public void unregisterCondition(String name) {
        conditions.remove(name);
        log.info("Unregistered user-defined condition: {}", name);
    }

    /**
     * Get all registered condition names.
     *
     * @return a map of all registered conditions
     */
    public Map<String, UserDefinedConditionDefinition> getAllConditions() {
        return Map.copyOf(conditions);
    }
}

