package com.davidrandoll.automation.engine.spring.modules.conditions.on_event_type;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
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

    @ContextField(
        placeholder = "com.example.events.OrderCreatedEvent",
        helpText = "Fully qualified class name of the event to match"
    )
    private String eventType;

    @ContextField(
        placeholder = "OrderCreatedEvent",
        helpText = "Simple class name (without package). Use this or eventType, not both"
    )
    private String eventName;

    @ContextField(
        placeholder = ".*Order.*Event",
        helpText = "Regex pattern to match event class names"
    )
    private String regex;
}