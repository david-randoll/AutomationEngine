package com.davidrandoll.automation.engine.modules.results.basic;

import com.davidrandoll.automation.engine.core.result.IResultContext;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasicResultContext implements IResultContext {
    private String alias;
    private String description;

    @JsonAnySetter
    @JsonAnyGetter
    private JsonNode results;
}
