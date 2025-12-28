package com.davidrandoll.automation.engine.spring.modules.actions.uda;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
        UserDefinedActionContext.Fields.alias,
        UserDefinedActionContext.Fields.description,
        UserDefinedActionContext.Fields.name,
        UserDefinedActionContext.Fields.parameters
})
public class UserDefinedActionContext implements IActionContext {
    private String alias;
    private String description;

    @ContextField(
        placeholder = "myCustomAction",
        helpText = "Name of the registered user-defined action to invoke"
    )
    private String name;

    @ContextField(
        widget = ContextField.Widget.SWITCH,
        helpText = "If true, throws error when named action is not found. If false, skips silently"
    )
    private boolean throwErrorIfNotFound = true;

    @ContextField(
        placeholder = "result",
        helpText = "Variable name to store the action result for later use"
    )
    private String storeToVariable;

    @ContextField(
        helpText = "Parameters to pass to the user-defined action as key-value pairs"
    )
    @JsonAnyGetter
    @JsonAnySetter
    private Map<String, Object> parameters;
}