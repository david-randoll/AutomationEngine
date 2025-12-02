package com.davidrandoll.automation.engine.spring.modules.triggers.udt;

import com.davidrandoll.automation.engine.core.triggers.ITriggerContext;
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
        UserDefinedTriggerContext.Fields.alias,
        UserDefinedTriggerContext.Fields.description,
        UserDefinedTriggerContext.Fields.name,
        UserDefinedTriggerContext.Fields.parameters
})
public class UserDefinedTriggerContext implements ITriggerContext {
    private String alias;
    private String description;

    @JsonAlias({"name"})
    private String name;

    @JsonAnyGetter
    @JsonAnySetter
    private Map<String, Object> parameters;
}

