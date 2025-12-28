package com.davidrandoll.automation.engine.spring.events.modules.publish_spring_event;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldNameConstants
@JsonPropertyOrder({
        PublishSpringEventActionContext.Fields.alias,
        PublishSpringEventActionContext.Fields.description,
        PublishSpringEventActionContext.Fields.className,
        PublishSpringEventActionContext.Fields.publishToAutomationEngine,
        PublishSpringEventActionContext.Fields.data
})
public class PublishSpringEventActionContext implements IActionContext {
    /** Unique identifier for this action */
    private String alias;

    /** Human-readable description of what this action does */
    private String description;

    /** Fully qualified class name of the event to publish (must implement IEvent) */
    @ContextField(
        placeholder = "com.example.events.OrderCreatedEvent",
        helpText = "Fully qualified class name of the event to publish (must implement IEvent)"
    )
    private String className;

    /** Whether to publish the event to AutomationEngine for processing by other automations. Defaults to false */
    @ContextField(
        widget = ContextField.Widget.SWITCH,
        helpText = "If true, the event will trigger other automations registered in AutomationEngine"
    )
    @JsonAlias({"publishToAE", "publishToAutomationEngine", "ae"})
    private boolean publishToAutomationEngine = false;

    /** Event data as key-value pairs. These will be set as properties on the event instance */
    @ContextField(
        helpText = "Event properties as key-value pairs. These will be set on the event instance"
    )
    @JsonAnySetter
    @JsonAnyGetter
    private Map<String, Object> data;
}
