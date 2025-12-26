package com.davidrandoll.automation.engine.core.variables;

import com.davidrandoll.automation.engine.creator.variables.VariableDefinition;
import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
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

    public VariableContext(String alias, String description, String type, Map<String, Object> data) {
        this.alias = alias;
        this.description = description;
        this.variable = type;
        this.data = data != null ? data : new HashMap<>();
    }

    public VariableContext(String alias, String description, String action, Map<String, Object> data, Map<String, Object> options) {
        this(alias, description, action, data);
        this.options = options;
    }

    public VariableContext(VariableDefinition definition) {
        this(definition.getAlias(), definition.getDescription(), definition.getVariable(), definition.getParams(), definition.getOptions());
    }

    public VariableContext(VariableContext other, Map<String, Object> additionalData) {
        this(other.getAlias(), other.getDescription(), other.getVariable(), additionalData, other.getOptions());
    }

    public VariableContext(VariableContext other) {
        this(other, new HashMap<>(other.getData()));
    }

    public VariableContext changeData(Map<String, Object> newData) {
        this.data = newData;
        return this;
    }
}