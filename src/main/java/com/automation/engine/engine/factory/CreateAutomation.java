package com.automation.engine.engine.factory;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAutomation {
    private String alias;
    private List<Trigger> triggers = new ArrayList<>();
    private List<Condition> conditions = new ArrayList<>();
    private List<Action> actions = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Trigger {
        private String alias;
        private String trigger;

        @JsonIgnore
        @JsonAnyGetter
        @JsonAnySetter
        private Map<String, Object> params = new HashMap<>();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Condition {
        private String alias;
        private String condition;

        @JsonIgnore
        @JsonAnyGetter
        @JsonAnySetter
        private Map<String, Object> params = new HashMap<>();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Action {
        private String alias;
        private String action;

        @JsonIgnore
        @JsonAnyGetter
        @JsonAnySetter
        private Map<String, Object> params = new HashMap<>();
    }
}