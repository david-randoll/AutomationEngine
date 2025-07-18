package com.davidrandoll.automation.engine.creator.variables;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VariableDefinition {
    private String alias;

    @JsonAlias({"variable", "type"})
    private String variable = "basic";

    @JsonIgnore
    @JsonAnyGetter
    @JsonAnySetter
    private Map<String, Object> params = new HashMap<>();
}
