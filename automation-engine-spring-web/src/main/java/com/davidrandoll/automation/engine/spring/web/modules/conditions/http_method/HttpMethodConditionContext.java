package com.davidrandoll.automation.engine.spring.web.modules.conditions.http_method;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.davidrandoll.automation.engine.spring.web.modules.conditions.MatchContext;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants
@JsonPropertyOrder({
        HttpMethodConditionContext.Fields.alias,
        HttpMethodConditionContext.Fields.description
})
public class HttpMethodConditionContext extends MatchContext implements IConditionContext {
    /** Unique identifier for this condition */
    private String alias;

    /** Human-readable description of what this condition checks */
    private String description;
}