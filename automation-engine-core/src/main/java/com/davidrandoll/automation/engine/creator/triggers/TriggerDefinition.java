package com.davidrandoll.automation.engine.creator.triggers;

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
        TriggerDefinition.Fields.alias,
        TriggerDefinition.Fields.description,
        TriggerDefinition.Fields.trigger
})
public class TriggerDefinition {
    private String alias;
    private String description;

    @NotEmpty
    @JsonAlias({"type", "trigger"})
    private String trigger;

    @JsonIgnore
    @JsonAnyGetter
    @JsonAnySetter
    private Map<String, Object> params = new HashMap<>();
}
