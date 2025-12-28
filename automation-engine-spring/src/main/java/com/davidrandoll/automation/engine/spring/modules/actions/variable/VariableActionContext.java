package com.davidrandoll.automation.engine.spring.modules.actions.variable;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        VariableActionContext.Fields.alias,
        VariableActionContext.Fields.description
})
public class VariableActionContext implements IActionContext {
    /** Unique identifier for this action */
    private String alias;

    /** Human-readable description of what this action does */
    private String description;

    /** Map of variable names to values. Any additional properties will be stored as variables in the automation context */
    @ContextField(
        helpText = "Define variables as key-value pairs. Values support {{ }} templates. Access via {{ variableName }}"
    )
    @JsonIgnore
    @JsonAnySetter
    @JsonAnyGetter
    private Map<String, Object> variables = new HashMap<>();
}
