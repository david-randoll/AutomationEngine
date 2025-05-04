package com.davidrandoll.automation.engine.modules.actions.repeat;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.creator.actions.Action;
import com.davidrandoll.automation.engine.creator.conditions.Condition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepeatActionContext implements IActionContext {
    private String alias;
    // repeat options
    private long count;

    @JsonProperty("while")
    private List<Condition> whileConditions;

    @JsonProperty("until")
    private List<Condition> untilConditions;

    @JsonProperty("for_each")
    private List<Object> forEach;

    // actions to repeat
    private List<Action> actions;

    public boolean hasWhileConditions() {
        return !ObjectUtils.isEmpty(whileConditions);
    }

    public boolean hasUntilConditions() {
        return !ObjectUtils.isEmpty(untilConditions);
    }

    public boolean hasForEach() {
        return !ObjectUtils.isEmpty(forEach);
    }
}