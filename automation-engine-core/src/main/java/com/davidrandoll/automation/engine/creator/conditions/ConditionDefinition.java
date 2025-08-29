package com.davidrandoll.automation.engine.creator.conditions;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.NotEmpty;
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
        ConditionDefinition.Fields.alias,
        ConditionDefinition.Fields.description,
        ConditionDefinition.Fields.condition
})
public class ConditionDefinition {
    private String alias;
    private String description;

    @NotEmpty
    @JsonAlias({"type", "condition"})
    private String condition;

    @JsonIgnore
    @JsonAnyGetter
    @JsonAnySetter
    private Map<String, Object> params = new HashMap<>();
}