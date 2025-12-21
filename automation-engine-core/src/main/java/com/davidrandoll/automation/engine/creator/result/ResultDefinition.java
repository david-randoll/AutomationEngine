package com.davidrandoll.automation.engine.creator.result;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldNameConstants
@JsonPropertyOrder({
        ResultDefinition.Fields.alias,
        ResultDefinition.Fields.description,
        ResultDefinition.Fields.result
})
public class ResultDefinition {
    private String alias;
    private String description;

    @JsonAlias({"result", "return", "type"})
    private String result = "basic";

    @JsonIgnore
    @JsonAnySetter
    @JsonProperty("0829b1b94f764e47b871865ea6628f34")
    private JsonNode params;
}