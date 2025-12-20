package com.davidrandoll.automation.engine.creator;

import com.davidrandoll.automation.engine.creator.actions.ActionDefinition;
import com.davidrandoll.automation.engine.creator.conditions.ConditionDefinition;
import com.davidrandoll.automation.engine.creator.result.ResultDefinition;
import com.davidrandoll.automation.engine.creator.result.ResultDeserializer;
import com.davidrandoll.automation.engine.creator.triggers.TriggerDefinition;
import com.davidrandoll.automation.engine.creator.variables.VariableDefinition;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldNameConstants
@JsonPropertyOrder({
        AutomationDefinition.Fields.alias,
        AutomationDefinition.Fields.description,
        AutomationDefinition.Fields.options,
        AutomationDefinition.Fields.variables,
        AutomationDefinition.Fields.triggers,
        AutomationDefinition.Fields.conditions,
        AutomationDefinition.Fields.actions,
        AutomationDefinition.Fields.result
})
public class AutomationDefinition {
    @Getter
    private String alias;
    @Getter
    private String description;

    @Getter
    @Builder.Default
    private Map<String, Object> options = new HashMap<>();

    private List<VariableDefinition> variables = new ArrayList<>();
    private List<TriggerDefinition> triggers = new ArrayList<>();
    private List<ConditionDefinition> conditions = new ArrayList<>();
    private List<ActionDefinition> actions = new ArrayList<>();

    @JsonAlias({"execution_summary", "result", "summary", "executionResult", "return"})
    @JsonDeserialize(using = ResultDeserializer.class)
    private ResultDefinition result = new ResultDefinition();

    public List<VariableDefinition> getVariables() {
        return getNonNullList(variables);
    }

    public List<TriggerDefinition> getTriggers() {
        return getNonNullList(triggers);
    }

    public List<ConditionDefinition> getConditions() {
        return getNonNullList(conditions);
    }

    public List<ActionDefinition> getActions() {
        return getNonNullList(actions);
    }

    public ResultDefinition getResult() {
        return Optional.ofNullable(result).orElse(new ResultDefinition());
    }

    private <T> List<T> getNonNullList(List<T> list) {
        return Optional.ofNullable(list)
                .orElse(new ArrayList<>())
                .stream()
                .filter(Objects::nonNull)
                .toList();
    }
}