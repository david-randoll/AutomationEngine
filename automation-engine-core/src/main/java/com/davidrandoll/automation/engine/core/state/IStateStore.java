package com.davidrandoll.automation.engine.core.state;

import com.davidrandoll.automation.engine.core.events.EventContext;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface for persisting and retrieving the state of paused automations.
 */
public interface IStateStore {
    /**
     * Saves the state of a paused automation.
     *
     * @param context The event context to save.
     */
    void save(EventContext context);

    /**
     * Retrieves the state of a paused automation by its execution ID.
     *
     * @param executionId The execution ID of the automation.
     * @return An Optional containing the event context if found, or empty otherwise.
     */
    Optional<EventContext> findById(UUID executionId);

    /**
     * Retrieves all paused automation states.
     *
     * @return A list of all paused event contexts.
     */
    List<EventContext> findAll();

    /**
     * Removes the state of a paused automation.
     *
     * @param executionId The execution ID of the automation.
     */
    void remove(UUID executionId);
}
