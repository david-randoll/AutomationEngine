package com.davidrandoll.automation.engine.spring.modules.conditions.udc;

import com.davidrandoll.automation.engine.creator.conditions.ConditionDefinition;
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
 * Defines a user-defined condition.
 * Users can create their own reusable conditions with parameters.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldNameConstants
@JsonPropertyOrder({
        UserDefinedConditionDefinition.Fields.name,
        UserDefinedConditionDefinition.Fields.description,
        UserDefinedConditionDefinition.Fields.parameters,
        UserDefinedConditionDefinition.Fields.variables,
        UserDefinedConditionDefinition.Fields.conditions
})
public class UserDefinedConditionDefinition {
    private String name;
    private String description;

    /**
     * Parameter definitions for this condition (name -> default value or description)
     */
    private Map<String, Object> parameters;

    /**
     * Variables to be resolved when this condition is evaluated
     */
    private List<VariableDefinition> variables = new ArrayList<>();

    /**
     * Conditions to evaluate (all must be satisfied)
     */
    private List<ConditionDefinition> conditions = new ArrayList<>();
}


