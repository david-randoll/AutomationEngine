package com.davidrandoll.automation.engine.spring.modules.actions.uda;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
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
        UserDefinedActionContext.Fields.alias,
        UserDefinedActionContext.Fields.description,
        UserDefinedActionContext.Fields.name,
        UserDefinedActionContext.Fields.parameters
})
public class UserDefinedActionContext implements IActionContext {
    private String alias;
    private String description;

    @JsonAlias({"name"})
    private String name;

    @JsonAnyGetter
    @JsonAnySetter
    private Map<String, Object> parameters;
}