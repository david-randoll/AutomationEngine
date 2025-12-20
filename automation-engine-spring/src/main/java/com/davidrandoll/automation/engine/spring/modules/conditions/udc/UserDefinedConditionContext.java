package com.davidrandoll.automation.engine.spring.modules.conditions.udc;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
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
        UserDefinedConditionContext.Fields.alias,
        UserDefinedConditionContext.Fields.description,
        UserDefinedConditionContext.Fields.name,
        UserDefinedConditionContext.Fields.parameters
})
public class UserDefinedConditionContext implements IConditionContext {
    private String alias;
    private String description;

    @JsonAlias({"name"})
    private String name;

    private boolean throwErrorIfNotFound = true;

    @JsonAnyGetter
    @JsonAnySetter
    private Map<String, Object> parameters;
}

