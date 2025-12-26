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
        TriggerDefinition.Fields.options,
        TriggerDefinition.Fields.trigger
})
public class TriggerDefinition {
    private String alias;
    private String description;
    private Map<String, Object> options = new HashMap<>();

    @NotEmpty
    @JsonAlias({"type", "trigger"})
    private String trigger;

    @JsonIgnore
    @JsonAnyGetter
    @JsonAnySetter
    @JsonProperty("0829b1b94f764e47b871865ea6628f34")
    private Map<String, Object> params = new HashMap<>();
}
