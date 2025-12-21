package com.davidrandoll.automation.engine.core.variables;

import com.davidrandoll.automation.engine.creator.variables.VariableDefinition;
import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VariableContext {
    private String alias;
    private String description;

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
        this.variable = definition.getVariable();
        this.data = definition.getParams();
    }

    public VariableContext(VariableContext other, Map<String, Object> additionalData) {
        this.alias = other.getAlias();
        this.description = other.getDescription();
        this.variable = other.getVariable();
        this.data = additionalData;
    }
}