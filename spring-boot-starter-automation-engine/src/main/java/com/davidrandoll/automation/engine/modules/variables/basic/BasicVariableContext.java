package com.davidrandoll.automation.engine.modules.variables.basic;

import com.davidrandoll.automation.engine.core.variables.IVariableContext;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        BasicVariableContext.Fields.alias,
        BasicVariableContext.Fields.description
})
public class BasicVariableContext implements IVariableContext {
    private String alias;
    private String description;

    @JsonIgnore
    @JsonAnySetter
    @JsonAnyGetter
    private Map<String, Object> variables = new HashMap<>();
}
