package com.davidrandoll.automation.engine.spring.modules.results.basic;

import com.davidrandoll.automation.engine.core.result.IResultContext;
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
    private String alias;
    private String description;

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