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
public class ConditionContext implements IConditionContext {
    private String alias;
    private String description;
    private Map<String, Object> options = new HashMap<>();

    @NotEmpty
    @JsonIgnore
    @JsonAlias({"type", "condition"})
    private String condition;

    @JsonIgnore
    @JsonAnyGetter
    @JsonAnySetter
    @JsonProperty("0829b1b94f764e47b871865ea6628f34")
    private Map<String, Object> data;

    public ConditionContext(String alias, String description, String type, Map<String, Object> data) {
        this.alias = alias;
        this.description = description;
        this.condition = type;
        this.data = data != null ? data : new HashMap<>();
    }

    public ConditionContext(String alias, String description, String action, Map<String, Object> data, Map<String, Object> options) {
        this(alias, description, action, data);
        this.options = options;
    }

    public ConditionContext(ConditionDefinition definition) {
        this(definition.getAlias(), definition.getDescription(), definition.getCondition(), definition.getParams(), definition.getOptions());
    }

    public ConditionContext(ConditionContext other, Map<String, Object> additionalData) {
        this(other.getAlias(), other.getDescription(), other.getCondition(), additionalData, other.getOptions());
    }

    public ConditionContext(ConditionContext other) {
        this(other, new HashMap<>(other.getData()));
    }

    public ConditionContext changeData(Map<String, Object> newData) {
        this.data = newData;
        return this;
    }
}