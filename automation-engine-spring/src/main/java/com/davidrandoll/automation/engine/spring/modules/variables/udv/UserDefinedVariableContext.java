package com.davidrandoll.automation.engine.spring.modules.variables.udv;

import com.davidrandoll.automation.engine.core.variables.IVariableContext;
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
        UserDefinedVariableContext.Fields.alias,
        UserDefinedVariableContext.Fields.description,
        UserDefinedVariableContext.Fields.name,
        UserDefinedVariableContext.Fields.parameters
})
public class UserDefinedVariableContext implements IVariableContext {
    private String alias;
    private String description;

    @ContextField(
        placeholder = "myCustomVariable",
        helpText = "Name of the registered user-defined variable resolver to invoke"
    )
    @JsonAlias({"name"})
    private String name;

    @ContextField(
        widget = ContextField.Widget.SWITCH,
        helpText = "If true, throws error when named variable is not found. If false, returns null silently"
    )
    private boolean throwErrorIfNotFound = true;

    @ContextField(
        helpText = "Parameters to pass to the user-defined variable resolver as key-value pairs"
    )
    @JsonAnyGetter
    @JsonAnySetter
    private Map<String, Object> parameters;
}

