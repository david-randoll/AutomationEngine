package com.davidrandoll.automation.engine.spring.modules.results.basic;

import com.davidrandoll.automation.engine.core.result.IResultContext;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        BasicResultContext.Fields.alias,
        BasicResultContext.Fields.description,
        BasicResultContext.Fields.results
})
public class BasicResultContext implements IResultContext {
    /** Unique identifier for this result */
    private String alias;

    /** Human-readable description of what this result represents */
    private String description;

    /** JSON object containing the result data. Any additional properties will be included in the result */
    @ContextField(
        helpText = "Define result properties as key-value pairs. Values can use {{ }} templates"
    )
    @JsonIgnore
    @JsonAnySetter
    private JsonNode results;

    @JsonAnyGetter
    public Map<String, Object> getResultsAsMap() {
        if (results == null || results.isNull()) {
            return Map.of();
        }
        // loop through the fields and convert to map
        var map = new HashMap<String, Object>();
        results.fieldNames()
                .forEachRemaining(field -> map.put(field, results.get(field)));
        return map;
    }
}