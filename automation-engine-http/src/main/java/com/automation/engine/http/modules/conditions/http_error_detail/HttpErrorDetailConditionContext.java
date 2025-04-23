package com.automation.engine.http.modules.conditions.http_error_detail;

import com.automation.engine.core.conditions.IConditionContext;
import com.automation.engine.http.modules.conditions.StringMatchContext;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class HttpErrorDetailConditionContext implements IConditionContext {
    private String alias;

    @JsonAnySetter
    @JsonAnyGetter
    private Map<String, StringMatchContext> errorDetail;
}