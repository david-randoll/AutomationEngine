package com.davidrandoll.automation.engine.spring.modules.actions.repeat;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.creator.actions.ActionDefinition;
import com.davidrandoll.automation.engine.creator.conditions.ConditionDefinition;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldNameConstants
@JsonPropertyOrder({
        RepeatActionContext.Fields.alias,
        RepeatActionContext.Fields.description,
        RepeatActionContext.Fields.count,
        RepeatActionContext.Fields.whileConditions,
        RepeatActionContext.Fields.untilConditions,
        RepeatActionContext.Fields.forEach,
        RepeatActionContext.Fields.as,
        RepeatActionContext.Fields.actions
})
public class RepeatActionContext implements IActionContext {
    /**
     * Unique identifier for this action
     */
    private String alias;

    /**
     * Human-readable description of what this action does
     */
    private String description;

    /**
     * Number of times to repeat the actions. Use this for a fixed number of iterations
     */
    private long count;

    /**
     * Conditions to check before each iteration. Actions repeat while all conditions are true
     */
    @JsonProperty("while")
    private List<ConditionDefinition> whileConditions;

    /**
     * Conditions to check after each iteration. Actions repeat until all conditions become true
     */
    @JsonProperty("until")
    private List<ConditionDefinition> untilConditions;

    /**
     * List or array to iterate over. Actions execute once for each item, with the current item available in the context
     */
    @JsonAlias({"for_each", "forEach", "foreach"})
    private JsonNode forEach;

    /**
     * Variable name to use for the current item in forEach loops. Defaults to "item" if not specified
     */
    private String as = "item";

    /**
     * Actions to execute on each iteration of the repeat loop
     */
    private List<ActionDefinition> actions;

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