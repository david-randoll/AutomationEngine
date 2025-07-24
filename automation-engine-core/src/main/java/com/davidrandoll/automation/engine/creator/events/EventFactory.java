package com.davidrandoll.automation.engine.creator.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class EventFactory {
    private final ObjectMapper mapper;

    public JsonEvent createEvent(JsonNode event) {
        return new JsonEvent(event);
    }

    public JsonEvent createEvent(Object event) {
        if (event == null) {
            throw new IllegalArgumentException("Cannot create event from null object");
        }
        log.debug("Creating event from object: {}", event.getClass().getSimpleName());
        JsonNode jsonNode = mapper.valueToTree(event);
        var result = new JsonEvent(jsonNode);
        result.setEventType(event.getClass().getName());
        return result;
    }
}