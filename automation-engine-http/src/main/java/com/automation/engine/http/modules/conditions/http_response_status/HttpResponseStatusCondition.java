package com.automation.engine.http.modules.conditions.http_response_status;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.event.HttpResponseEvent;
import com.automation.engine.http.utils.StringMatcher;
import com.automation.engine.spi.PluggableCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("httpResponseStatusCondition")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "httpResponseStatusCondition", ignored = HttpResponseStatusCondition.class)
public class HttpResponseStatusCondition extends PluggableCondition<HttpResponseStatusConditionContext> {
    private final ObjectMapper objectMapper;

    @Override
    public boolean isSatisfied(EventContext ec, HttpResponseStatusConditionContext cc) {
        if (!(ec.getEvent() instanceof HttpResponseEvent event)) return false;
        if (event.getResponseStatus() != null) {
            var statusFamily = switch (event.getResponseStatus().series()) {
                case INFORMATIONAL -> "1xx";
                case SUCCESSFUL -> "2xx";
                case REDIRECTION -> "3xx";
                case CLIENT_ERROR -> "4xx";
                case SERVER_ERROR -> "5xx";
            };

            var statuses = List.of(
                    event.getResponseStatus().getReasonPhrase(),
                    event.getResponseStatus().name(),
                    event.getResponseStatus().toString(),
                    String.valueOf(event.getResponseStatus().value()),
                    statusFamily
            );
            return StringMatcher.matchesCondition(cc, statuses, objectMapper);
        }
        return StringMatcher.matchesCondition(cc, null, objectMapper);
    }
}