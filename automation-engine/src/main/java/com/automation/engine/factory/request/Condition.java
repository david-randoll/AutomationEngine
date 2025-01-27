package com.automation.engine.factory.request;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Condition {
    private String alias;
    private String condition;

    @JsonIgnore
    @JsonAnyGetter
    @JsonAnySetter
    private Map<String, Object> params = new HashMap<>();
}