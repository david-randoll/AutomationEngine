package com.automation.engine.http.modules.conditions.http_error_detail;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.event.HttpResponseEvent;
import com.automation.engine.http.utils.StringMatcher;
import com.automation.engine.spi.PluggableCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component("httpErrorDetailCondition")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "httpErrorDetailCondition", ignored = HttpErrorDetailCondition.class)
public class HttpErrorDetailCondition extends PluggableCondition<HttpErrorDetailConditionContext> {
    private final ObjectMapper objectMapper;

    @Override
    public boolean isSatisfied(EventContext ec, HttpErrorDetailConditionContext cc) {
        if (!(ec.getEvent() instanceof HttpResponseEvent event)) return false;
        return StringMatcher.matchesCondition(cc.getErrorDetail(), event.getErrorDetail(), objectMapper);
    }
}