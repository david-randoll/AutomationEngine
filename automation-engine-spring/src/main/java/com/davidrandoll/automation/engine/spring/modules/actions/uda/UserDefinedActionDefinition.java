package com.davidrandoll.automation.engine.spring.modules.actions.uda;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.creator.actions.ActionDefinition;
import com.davidrandoll.automation.engine.creator.conditions.ConditionDefinition;
import com.davidrandoll.automation.engine.creator.result.ResultDefinition;
import com.davidrandoll.automation.engine.creator.result.ResultDeserializer;
import com.davidrandoll.automation.engine.creator.variables.VariableDefinition;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.util.ArrayList;
import java.util.HashMap;
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

    private String name;
    private Map<String, Object> parameters;

    @Getter
    @Builder.Default
    private Map<String, Object> options = new HashMap<>();

    private List<VariableDefinition> variables = new ArrayList<>();
    private List<ConditionDefinition> conditions = new ArrayList<>();
    private List<ActionDefinition> actions = new ArrayList<>();

    @JsonAlias({"execution_summary", "result", "summary", "executionResult", "return"})
    @JsonDeserialize(using = ResultDeserializer.class)
    private ResultDefinition result = new ResultDefinition();
}


