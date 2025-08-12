package com.davidrandoll.automation.engine.modules.conditions.always_true;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        AlwaysTrueConditionContext.Fields.alias,
        AlwaysTrueConditionContext.Fields.description
})
public class AlwaysTrueConditionContext implements IConditionContext {
    private String alias;
    private String description;
}
