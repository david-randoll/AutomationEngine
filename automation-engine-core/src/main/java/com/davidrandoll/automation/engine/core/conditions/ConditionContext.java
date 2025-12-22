package com.davidrandoll.automation.engine.core.conditions;

import com.davidrandoll.automation.engine.creator.conditions.ConditionDefinition;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConditionContext {
    private String alias;
    private String description;

    @NotEmpty
    @JsonIgnore
    @JsonAlias({"type", "condition"})
    private String condition;

    @JsonIgnore
    @JsonAnyGetter
    @JsonAnySetter
    @JsonProperty("0829b1b94f764e47b871865ea6628f34")
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

    public ConditionContext(ConditionContext other) {
        this(other, new HashMap<>(other.getData()));
    }

    public ConditionContext changeData(Map<String, Object> newData) {
        this.data = newData;
        return this;
    }
}