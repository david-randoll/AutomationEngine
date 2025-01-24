package com.automation.engine.engine.factory;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAutomation {
    private String alias;
    private List<Trigger> triggers;
    private List<Condition> conditions;
    private List<Action> actions;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Trigger {
        private String alias;
        private String trigger;

        @JsonIgnore
        @JsonAnyGetter
        @JsonAnySetter
        private Map<String, Object> params;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Condition {
        private String alias;
        private String condition;

        @JsonIgnore
        @JsonAnyGetter
        @JsonAnySetter
        private Map<String, Object> params;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Action {
        private String alias;
        private String action;

        @JsonIgnore
        @JsonAnyGetter
        @JsonAnySetter
        private Map<String, Object> params;
    }
}