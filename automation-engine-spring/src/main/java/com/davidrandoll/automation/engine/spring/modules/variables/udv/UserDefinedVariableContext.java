package com.davidrandoll.automation.engine.spring.modules.variables.udv;

import com.davidrandoll.automation.engine.core.variables.IVariableContext;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldNameConstants
@JsonPropertyOrder({
        UserDefinedVariableContext.Fields.alias,
        UserDefinedVariableContext.Fields.description,
        UserDefinedVariableContext.Fields.name,
        UserDefinedVariableContext.Fields.parameters
})
public class UserDefinedVariableContext implements IVariableContext {
    private String alias;
    private String description;

    @JsonAlias({"name"})
    private String name;

    @JsonAnyGetter
    @JsonAnySetter
    private Map<String, Object> parameters;
}

