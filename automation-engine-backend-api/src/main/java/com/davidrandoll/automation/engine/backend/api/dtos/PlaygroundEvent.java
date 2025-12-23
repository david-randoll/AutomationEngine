package com.davidrandoll.automation.engine.backend.api.dtos;

import com.davidrandoll.automation.engine.core.events.IEvent;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * A generic event that wraps a Map of input data.
 * Used by the playground to execute automations with arbitrary input data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaygroundEvent implements IEvent {
    @JsonAnyGetter
    private Map<String, Object> data = new HashMap<>();

    /**
     * Creates a PlaygroundEvent from the given input map.
     *
     * @param inputs the input data map
     * @return a new PlaygroundEvent
     */
    public static PlaygroundEvent from(Map<String, Object> inputs) {
        return new PlaygroundEvent(inputs != null ? inputs : new HashMap<>());
    }
}
