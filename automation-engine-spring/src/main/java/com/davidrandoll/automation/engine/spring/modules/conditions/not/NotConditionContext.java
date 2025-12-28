package com.davidrandoll.automation.engine.spring.modules.conditions.not;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.davidrandoll.automation.engine.creator.conditions.ConditionDefinition;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
        NotConditionContext.Fields.alias,
        NotConditionContext.Fields.description,
        NotConditionContext.Fields.conditions
})
public class NotConditionContext implements IConditionContext {
    private String alias;
    private String description;

    @ContextField(
        helpText = "Inverts the result. Passes if ALL inner conditions are FALSE (logical NOT)"
    )
    private List<ConditionDefinition> conditions = new ArrayList<>();
}