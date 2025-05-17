package com.davidrandoll.automation.engine.creator.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class EventFactory {
    private final ObjectMapper mapper;

    public JsonEvent createEvent(JsonNode body) {
        return new JsonEvent(body);
    }
}