package com.davidrandoll.automation.engine.core.variables;

import com.davidrandoll.automation.engine.creator.variables.VariableDefinition;
import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VariableContext implements IVariableContext {
    private String alias;
    private String description;
    private Map<String, Object> options = new HashMap<>();

    @JsonAlias({"variable", "type"})
    @JsonIgnore
    private String variable = "basic";

    @JsonIgnore
    @JsonAnyGetter
    @JsonAnySetter
    @JsonProperty("0829b1b94f764e47b871865ea6628f34")
    private Map<String, Object> data;

    public VariableContext(VariableDefinition definition) {
        this.alias = definition.getAlias();
        this.description = definition.getDescription();
        this.options = definition.getOptions();
        this.variable = definition.getVariable();
        this.data = definition.getParams();
    }

    public VariableContext(VariableContext other, Map<String, Object> additionalData) {
        this.alias = other.getAlias();
        this.description = other.getDescription();
        this.options = other.getOptions();
        this.variable = other.getVariable();
        this.data = additionalData;
    }

    public VariableContext(VariableContext other) {
        this(other, new HashMap<>(other.getData()));
    }

    public VariableContext changeData(Map<String, Object> newData) {
        this.data = newData;
        return this;
    }
}