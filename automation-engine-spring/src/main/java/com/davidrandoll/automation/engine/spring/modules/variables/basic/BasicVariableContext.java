package com.davidrandoll.automation.engine.spring.modules.variables.basic;

import com.davidrandoll.automation.engine.core.variables.IVariableContext;
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
        BasicVariableContext.Fields.alias,
        BasicVariableContext.Fields.description
})
public class BasicVariableContext implements IVariableContext {
    /** Unique identifier for this variable */
    private String alias;

    /** Human-readable description of what this variable represents */
    private String description;

    /** Map of variable names to values. Any additional properties will be stored as variables */
    @ContextField(
        helpText = "Define variables as key-value pairs. Values can use {{ }} templates. Access via {{ variableName }}"
    )
    @JsonIgnore
    @JsonAnySetter
    @JsonAnyGetter
    private Map<String, Object> variables = new HashMap<>();
}
