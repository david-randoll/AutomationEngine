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
    private String alias;
    private String description;
    private String eventType;
    private String eventName;
    private String regex;
}