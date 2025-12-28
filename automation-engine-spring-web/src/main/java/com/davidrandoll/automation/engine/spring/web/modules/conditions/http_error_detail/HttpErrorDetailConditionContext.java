package com.davidrandoll.automation.engine.spring.web.modules.conditions.http_error_detail;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
import com.davidrandoll.automation.engine.spring.web.modules.conditions.MatchContext;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.Map;

@Data
@NoArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        HttpErrorDetailConditionContext.Fields.alias,
        HttpErrorDetailConditionContext.Fields.description
})
public class HttpErrorDetailConditionContext implements IConditionContext {
    private String alias;
    private String description;

    @ContextField(
        helpText = "Map error detail fields (exception, message, etc.) to match conditions"
    )
    @JsonAnySetter
    @JsonAnyGetter
    private Map<String, MatchContext> errorDetail;
}