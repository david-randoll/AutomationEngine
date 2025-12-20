package com.davidrandoll.automation.engine.creator.result;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
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
    private JsonNode params;

    @JsonAnyGetter
    public Map<String, Object> getResultsAsMap() {
        if (params == null || params.isNull()) {
            return Map.of();
        }
        // loop through the fields and convert to map
        var map = new HashMap<String, Object>();
        params.fieldNames()
                .forEachRemaining(field -> map.put(field, params.get(field)));
        return map;
    }
}