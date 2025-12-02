package com.davidrandoll.automation.engine.spring.modules.variables.udv;

import com.davidrandoll.automation.engine.creator.variables.VariableDefinition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Defines a user-defined variable.
 * Users can create their own reusable variables with parameters.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldNameConstants
@JsonPropertyOrder({
        UserDefinedVariableDefinition.Fields.name,
        UserDefinedVariableDefinition.Fields.description,
        UserDefinedVariableDefinition.Fields.parameters,
        UserDefinedVariableDefinition.Fields.variables
})
public class UserDefinedVariableDefinition {
    private String name;
    private String description;

    /**
     * Parameter definitions for this variable (name -> default value or description)
     */
    private Map<String, Object> parameters;

    /**
     * Variables to be resolved
     */
    private List<VariableDefinition> variables = new ArrayList<>();
}


