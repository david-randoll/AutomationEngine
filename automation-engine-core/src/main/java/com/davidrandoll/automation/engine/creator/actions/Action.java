package com.davidrandoll.automation.engine.creator.actions;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotEmpty;
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
public class Action {
    private String alias;
    @NotEmpty
    private String action;

    @JsonIgnore
    @JsonAnyGetter
    @JsonAnySetter
    private Map<String, Object> params = new HashMap<>();
}
