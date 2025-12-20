package com.davidrandoll.automation.engine.core.conditions;

import com.davidrandoll.automation.engine.creator.conditions.ConditionDefinition;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConditionContext {
    private String alias;
    private String description;

    @NotEmpty
    @JsonAlias({"type", "condition"})
    private String condition;

    @JsonIgnore
    @JsonAnyGetter
    @JsonAnySetter
    private Map<String, Object> data;

    public ConditionContext(ConditionDefinition definition) {
        this.alias = definition.getAlias();
        this.description = definition.getDescription();
        this.condition = definition.getCondition();
        this.data = definition.getParams();
    }

    public ConditionContext(ConditionContext other, Map<String, Object> additionalData) {
        this.alias = other.getAlias();
        this.description = other.getDescription();
        this.condition = other.getCondition();
        this.data = additionalData;
    }
}