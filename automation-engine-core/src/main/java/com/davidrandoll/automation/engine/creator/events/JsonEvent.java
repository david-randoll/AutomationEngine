package com.davidrandoll.automation.engine.creator.events;

import com.davidrandoll.automation.engine.core.events.IEvent;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsonEvent implements IEvent {
    public static final String EVENT_TYPE = "eventType";
    private JsonNode event;

    @JsonAnyGetter
    public Map<String, JsonNode> getEventAsObject() {
        var result = new HashMap<String, JsonNode>();
        if (event != null) {
            if (event.isObject()) {
                event.fields().forEachRemaining(entry -> result.put(entry.getKey(), entry.getValue()));
            } else if (event.isArray()) {
                result.put("array", event);
            } else {
                result.put("text", event);
            }
        }
        return result;
    }

    public void setEventType(String eventType) {
        if (event == null || !event.isObject()) {
            throw new IllegalArgumentException("Event must be a JSON object to set event type");
        }
        ((ObjectNode) event).put(EVENT_TYPE, eventType);
    }

    @Override
    public String getEventType() {
        if (event != null && event.has(EVENT_TYPE)) {
            return event.get(EVENT_TYPE).asText();
        }
        return this.getClass().getName();
    }
}