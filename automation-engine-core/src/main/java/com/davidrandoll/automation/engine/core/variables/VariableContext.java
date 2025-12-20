package com.davidrandoll.automation.engine.core.variables;

import com.davidrandoll.automation.engine.creator.variables.VariableDefinition;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private String variable = "basic";

    @JsonIgnore
    @JsonAnyGetter
    @JsonAnySetter
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