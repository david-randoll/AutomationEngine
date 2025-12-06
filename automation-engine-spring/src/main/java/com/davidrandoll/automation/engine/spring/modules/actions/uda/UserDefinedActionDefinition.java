package com.davidrandoll.automation.engine.spring.modules.actions.uda;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.creator.actions.ActionDefinition;
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
 * Defines a user-defined action similar to AutomationDefinition.
 * Users can create their own reusable actions with parameters.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldNameConstants
@JsonPropertyOrder({
        UserDefinedActionDefinition.Fields.alias,
        UserDefinedActionDefinition.Fields.description,
        UserDefinedActionDefinition.Fields.name,
        UserDefinedActionDefinition.Fields.parameters,
        UserDefinedActionDefinition.Fields.variables,
        UserDefinedActionDefinition.Fields.conditions,
        UserDefinedActionDefinition.Fields.actions
})
public class UserDefinedActionDefinition implements IActionContext {
    private String alias;
    private String description;

    /**
     * Name of the user-defined action
     */
    private String name;
    /**
     * Parameter definitions for this action (name -> default value or description)
     */
    private Map<String, Object> parameters;

    /**
     * Variables to be resolved when this action executes
     */
    private List<VariableDefinition> variables = new ArrayList<>();

    /**
     * Conditions to check before executing
     */
    private List<ConditionDefinition> conditions = new ArrayList<>();

    /**
     * Actions to execute
     */
    private List<ActionDefinition> actions = new ArrayList<>();
}


