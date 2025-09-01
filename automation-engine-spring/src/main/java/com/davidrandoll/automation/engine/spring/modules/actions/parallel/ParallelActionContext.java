package com.davidrandoll.automation.engine.spring.modules.actions.parallel;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.creator.actions.ActionDefinition;
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
    private String alias;
    private String description;
    private List<ActionDefinition> actions;
}