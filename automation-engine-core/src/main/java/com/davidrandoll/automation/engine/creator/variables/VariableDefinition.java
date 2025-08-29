package com.davidrandoll.automation.engine.creator.variables;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldNameConstants
@JsonPropertyOrder({
        VariableDefinition.Fields.alias,
        VariableDefinition.Fields.description,
        VariableDefinition.Fields.variable
})
public class VariableDefinition {
    private String alias;
    private String description;

    @JsonAlias({"variable", "type"})
    private String variable = "basic";

    @JsonIgnore
    @JsonAnyGetter
    @JsonAnySetter
    private Map<String, Object> params = new HashMap<>();
}
