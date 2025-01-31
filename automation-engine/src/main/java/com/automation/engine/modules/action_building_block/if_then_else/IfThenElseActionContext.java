package com.automation.engine.modules.action_building_block.if_then_else;

import com.automation.engine.factory.request.Action;
import com.automation.engine.factory.request.Condition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IfThenElseActionContext {
    @JsonProperty("if")
    private If ifBlock;

    @JsonProperty("then")
    private Then thenBlock;

    @JsonProperty("else")
    private Else elseBlock;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class If {
        private List<Condition> conditions;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Then {
        private List<Action> actions;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Else {
        private List<Action> actions;
    }
}
