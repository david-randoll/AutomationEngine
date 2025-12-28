package com.davidrandoll.automation.engine.spring.modules.triggers.udt;

import com.davidrandoll.automation.engine.core.triggers.ITriggerContext;
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
        UserDefinedTriggerContext.Fields.alias,
        UserDefinedTriggerContext.Fields.description,
        UserDefinedTriggerContext.Fields.name,
        UserDefinedTriggerContext.Fields.parameters
})
public class UserDefinedTriggerContext implements ITriggerContext {
    private String alias;
    private String description;

    @ContextField(
        placeholder = "myCustomTrigger",
        helpText = "Name of the registered user-defined trigger to invoke"
    )
    @JsonAlias({"name"})
    private String name;

    @ContextField(
        widget = ContextField.Widget.SWITCH,
        helpText = "If true, throws error when named trigger is not found. If false, returns false silently"
    )
    private boolean throwErrorIfNotFound = true;

    @ContextField(
        helpText = "Parameters to pass to the user-defined trigger as key-value pairs"
    )
    @JsonAnyGetter
    @JsonAnySetter
    private Map<String, Object> parameters;
}

