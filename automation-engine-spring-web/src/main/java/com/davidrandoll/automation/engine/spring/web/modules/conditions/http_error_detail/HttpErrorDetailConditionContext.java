package com.davidrandoll.automation.engine.spring.web.modules.conditions.http_error_detail;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.davidrandoll.automation.engine.spring.web.modules.conditions.MatchContext;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class HttpErrorDetailConditionContext implements IConditionContext {
    private String alias;
    private String description;

    @JsonAnySetter
    @JsonAnyGetter
    private Map<String, MatchContext> errorDetail;
}