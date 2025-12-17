package com.davidrandoll.automation.engine.spring.modules.triggers.on_event_type;

import com.davidrandoll.automation.engine.core.triggers.ITriggerContext;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldNameConstants
@JsonPropertyOrder({
        OnEventTypeTriggerContext.Fields.alias,
        OnEventTypeTriggerContext.Fields.description,
        OnEventTypeTriggerContext.Fields.eventType,
        OnEventTypeTriggerContext.Fields.eventName,
        OnEventTypeTriggerContext.Fields.regex
})
public class OnEventTypeTriggerContext implements ITriggerContext {
    /** Unique identifier for this trigger */
    private String alias;

    /** Human-readable description of what this trigger responds to */
    private String description;

    /** Fully qualified class name of the event type to match (e.g., com.example.MyEvent) */
    private String eventType;

    /** Simple name of the event class to match (e.g., MyEvent). Alternative to eventType */
    private String eventName;

    /** Regular expression pattern to match against the event class name */
    private String regex;
}