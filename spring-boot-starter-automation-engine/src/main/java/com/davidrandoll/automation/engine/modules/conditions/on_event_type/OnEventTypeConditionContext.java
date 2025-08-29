package com.davidrandoll.automation.engine.modules.conditions.on_event_type;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
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
        OnEventTypeConditionContext.Fields.alias,
        OnEventTypeConditionContext.Fields.description,
        OnEventTypeConditionContext.Fields.eventType,
        OnEventTypeConditionContext.Fields.eventName,
        OnEventTypeConditionContext.Fields.regex
})
public class OnEventTypeConditionContext implements IConditionContext {
    private String alias;
    private String description;
    private String eventType;
    private String eventName;
    private String regex;
}