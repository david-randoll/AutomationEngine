package com.davidrandoll.automation.engine.spring.modules.actions.parallel;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.creator.actions.ActionDefinition;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldNameConstants
@JsonPropertyOrder({
        ParallelActionContext.Fields.alias,
        ParallelActionContext.Fields.description,
        ParallelActionContext.Fields.actions
})
public class ParallelActionContext implements IActionContext {
    /** Unique identifier for this action */
    private String alias;

    /** Human-readable description of what this action does */
    private String description;

    /** List of actions to execute in parallel (concurrently) */
    @ContextField(
        helpText = "Actions execute concurrently. All start at the same time. Use for independent operations"
    )
    private List<ActionDefinition> actions;
}