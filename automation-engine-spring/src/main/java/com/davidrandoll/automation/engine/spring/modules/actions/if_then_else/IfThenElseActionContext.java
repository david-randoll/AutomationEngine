package com.davidrandoll.automation.engine.spring.modules.actions.if_then_else;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.creator.actions.ActionDefinition;
import com.davidrandoll.automation.engine.creator.conditions.ConditionDefinition;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldNameConstants
@JsonPropertyOrder({
        IfThenElseActionContext.Fields.alias,
        IfThenElseActionContext.Fields.description,
        IfThenElseActionContext.Fields.ifConditions,
        IfThenElseActionContext.Fields.thenActions,
        IfThenElseActionContext.Fields.ifThenBlocks,
        IfThenElseActionContext.Fields.elseActions
})
public class IfThenElseActionContext implements IActionContext {
    /** Unique identifier for this action */
    private String alias;

    /** Human-readable description of what this action does */
    private String description;

    /** Conditions to evaluate for the primary if block. All conditions must be true */
    @ContextField(
        helpText = "Primary conditions to evaluate. ALL must be true to execute 'then' actions"
    )
    @JsonProperty("if")
    private List<ConditionDefinition> ifConditions = new ArrayList<>();

    /** Actions to execute if the primary if conditions are all true */
    @ContextField(
        helpText = "Actions to execute when ALL 'if' conditions are true"
    )
    @JsonProperty("then")
    private List<ActionDefinition> thenActions = new ArrayList<>();

    /** Additional if-then blocks to evaluate in sequence (else-if logic) */
    @ContextField(
        helpText = "Additional else-if blocks evaluated in order when primary 'if' fails"
    )
    @JsonProperty("ifs")
    private List<IfThenBlock> ifThenBlocks = new ArrayList<>();

    /** Actions to execute if none of the if conditions are true */
    @ContextField(
        helpText = "Fallback actions when no if/else-if conditions match"
    )
    @JsonProperty("else")
    private List<ActionDefinition> elseActions = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldNameConstants
    @JsonPropertyOrder({
            IfThenBlock.Fields.alias,
            IfThenBlock.Fields.description,
            IfThenBlock.Fields.ifConditions,
            IfThenBlock.Fields.thenActions
    })
    public static class IfThenBlock implements IActionContext {
        /** Unique identifier for this if-then block */
        private String alias;

        /** Human-readable description of what this block checks */
        private String description;

        /** Conditions to evaluate for this if-then block. All conditions must be true */
        @ContextField(
            helpText = "Conditions for this else-if block. ALL must be true"
        )
        @JsonProperty("if")
        private List<ConditionDefinition> ifConditions = new ArrayList<>();

        /** Actions to execute if all conditions in this block are true */
        @ContextField(
            helpText = "Actions to execute when this block's conditions are met"
        )
        @JsonProperty("then")
        private List<ActionDefinition> thenActions = new ArrayList<>();
    }
}