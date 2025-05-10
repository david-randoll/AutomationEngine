package com.davidrandoll.automation.engine.modules.results.basic;

import com.davidrandoll.automation.engine.core.result.IResultContext;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasicResultContext implements IResultContext {
    private String alias;

    @JsonAnySetter
    @JsonAnyGetter
    private Map<String, Object> results = new HashMap<>();
}
