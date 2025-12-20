package com.davidrandoll.automation.engine.spring.modules.variables.udv;

import com.davidrandoll.automation.engine.core.variables.IVariableContext;
import com.fasterxml.jackson.annotation.*;
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

    private boolean throwErrorIfNotFound = true;

    @JsonAnyGetter
    @JsonAnySetter
    private Map<String, Object> parameters;
}

