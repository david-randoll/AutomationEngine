package com.davidrandoll.automation.engine.spring.modules.triggers.udt;

import com.davidrandoll.automation.engine.creator.triggers.TriggerDefinition;
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
 * Defines a user-defined trigger.
 * Users can create their own reusable triggers with parameters.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldNameConstants
@JsonPropertyOrder({
        UserDefinedTriggerDefinition.Fields.name,
        UserDefinedTriggerDefinition.Fields.description,
        UserDefinedTriggerDefinition.Fields.parameters,
        UserDefinedTriggerDefinition.Fields.variables,
        UserDefinedTriggerDefinition.Fields.triggers
})
public class UserDefinedTriggerDefinition {
    private String name;
    private String description;

    /**
     * Parameter definitions for this trigger (name -> default value or description)
     */
    private Map<String, Object> parameters;

    /**
     * Variables to be resolved when this trigger is evaluated
     */
    private List<VariableDefinition> variables = new ArrayList<>();

    /**
     * Triggers to evaluate (any must be triggered)
     */
    private List<TriggerDefinition> triggers = new ArrayList<>();
}


