package com.davidrandoll.automation.engine.core.state;

import com.davidrandoll.automation.engine.core.events.EventContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of IStateStore.
 */
public class InMemoryStateStore implements IStateStore {
    private final Map<UUID, EventContext> store = new ConcurrentHashMap<>();

    @Override
    public void save(EventContext context) {
        store.put(context.getExecutionId(), context);
    }

    @Override
    public Optional<EventContext> findById(UUID executionId) {
        return Optional.ofNullable(store.get(executionId));
    }

    @Override
    public List<EventContext> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void remove(UUID executionId) {
        store.remove(executionId);
    }
}
