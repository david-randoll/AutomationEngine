package com.davidrandoll.automation.engine.spring.modules.conditions.always_false;

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
        AlwaysFalseConditionContext.Fields.alias,
        AlwaysFalseConditionContext.Fields.description
})
public class AlwaysFalseConditionContext implements IConditionContext {
    private String alias;
    private String description;
}
