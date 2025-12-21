package com.davidrandoll.automation.engine.creator.actions;

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
        ActionDefinition.Fields.alias,
        ActionDefinition.Fields.description,
        ActionDefinition.Fields.action
})
public class ActionDefinition {
    private String alias;
    private String description;

    @NotEmpty
    @JsonAlias({"action", "type"})
    private String action;

    @JsonIgnore
    @JsonAnyGetter
    @JsonAnySetter
    @JsonProperty("0829b1b94f764e47b871865ea6628f34")
    private Map<String, Object> params = new HashMap<>();
}