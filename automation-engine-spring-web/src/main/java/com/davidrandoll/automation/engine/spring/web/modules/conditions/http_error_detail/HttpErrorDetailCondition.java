package com.davidrandoll.automation.engine.spring.web.modules.conditions.http_error_detail;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpResponseEvent;
import com.davidrandoll.automation.engine.spring.web.utils.StringMatcher;
import com.davidrandoll.automation.engine.spi.PluggableCondition;
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
        if (!(ec.getEvent() instanceof AEHttpResponseEvent event)) return false;
        return StringMatcher.matchesCondition(cc.getErrorDetail(), event.getErrorDetail(), objectMapper);
    }
}