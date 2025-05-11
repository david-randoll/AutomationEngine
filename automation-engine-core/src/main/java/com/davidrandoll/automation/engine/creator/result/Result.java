package com.davidrandoll.automation.engine.creator.result;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Result {
    private String alias;

    @JsonAlias({"result", "return", "type"})
    private String result = "basic";

    @JsonIgnore
    @JsonAnyGetter
    @JsonAnySetter
    private JsonNode params;
}