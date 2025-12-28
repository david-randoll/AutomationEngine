package com.davidrandoll.automation.engine.spring.modules.conditions.udc;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
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
        UserDefinedConditionContext.Fields.alias,
        UserDefinedConditionContext.Fields.description,
        UserDefinedConditionContext.Fields.name,
        UserDefinedConditionContext.Fields.parameters
})
public class UserDefinedConditionContext implements IConditionContext {
    private String alias;
    private String description;

    @ContextField(
        placeholder = "myCustomCondition",
        helpText = "Name of the registered user-defined condition to invoke"
    )
    @JsonAlias({"name"})
    private String name;

    @ContextField(
        widget = ContextField.Widget.SWITCH,
        helpText = "If true, throws error when named condition is not found. If false, returns false silently"
    )
    private boolean throwErrorIfNotFound = true;

    @ContextField(
        helpText = "Parameters to pass to the user-defined condition as key-value pairs"
    )
    @JsonAnyGetter
    @JsonAnySetter
    private Map<String, Object> parameters;
}

