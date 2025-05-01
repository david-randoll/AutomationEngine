package com.automation.engine.http.modules.triggers.on_http_response;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.event.HttpResponseEvent;
import com.automation.engine.http.utils.HttpServletUtils;
import com.automation.engine.http.utils.JsonNodeMatcher;
import com.automation.engine.spi.PluggableTrigger;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component("onHttpResponseTrigger")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "onHttpResponseTrigger", ignored = OnHttpResponseTrigger.class)
public class OnHttpResponseTrigger extends PluggableTrigger<OnHttpResponseTriggerContext> {
    private final ObjectMapper objectMapper;

    @Override
    public boolean isTriggered(EventContext ec, OnHttpResponseTriggerContext tc) {
        if (!(ec.getEvent() instanceof HttpResponseEvent event)) return false;

        var isMethodTriggered = tc.hasMethods() && !tc.getMethods().contains(event.getMethod());
        if (isMethodTriggered) return false;

        var fullUrlParsed = HttpServletUtils.normalizedUrl(event.getFullUrl());
        var isFullUrlTriggered = tc.hasFullPaths() && tc.getFullPaths().stream().noneMatch(fullUrlParsed::matches);
        if (isFullUrlTriggered) return false;

        var pathParsed = HttpServletUtils.normalizedUrl(event.getPath());
        var isPathTriggered = tc.hasPaths() && tc.getPaths().stream().noneMatch(pathParsed::matches);
        if (isPathTriggered) return false;

        var isHeaderTriggered = tc.hasHeaders() && JsonNodeMatcher.matches(tc.getHeaders(), event.getHeaders(), objectMapper);
        if (isHeaderTriggered) return false;

        var isQueryParamTriggered = tc.hasQueryParams() && JsonNodeMatcher.matches(tc.getQueryParams(), event.getQueryParams(), objectMapper);
        if (isQueryParamTriggered) return false;

        var isPathParamTriggered = tc.hasPathParams() && JsonNodeMatcher.matches(tc.getPathParams(), event.getPathParams(), objectMapper);
        if (isPathParamTriggered) return false;

        var isRequestBodyTriggered = tc.hasRequestBody() && JsonNodeMatcher.matches(tc.getRequestBody(), event.getRequestBody(), objectMapper);
        if (isRequestBodyTriggered) return false;

        var isResponseBodyTriggered = tc.hasResponseBody() && JsonNodeMatcher.matches(tc.getResponseBody(), event.getResponseBody(), objectMapper);
        if (isResponseBodyTriggered) return false;

        var isResponseStatusTriggered = tc.hasResponseStatuses() && !tc.getResponseStatuses().contains(event.getResponseStatus());
        if (isResponseStatusTriggered) return false;

        var errorDetailTriggered = tc.hasErrorDetail() && JsonNodeMatcher.matches(tc.getErrorDetail(), event.getErrorDetail(), objectMapper);
        if (errorDetailTriggered) return false;

        return true;
    }
}