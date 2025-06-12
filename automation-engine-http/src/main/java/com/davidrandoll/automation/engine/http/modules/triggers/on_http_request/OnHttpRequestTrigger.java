package com.davidrandoll.automation.engine.http.modules.triggers.on_http_request;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.http.utils.JsonNodeMatcher;
import com.davidrandoll.automation.engine.spi.PluggableTrigger;
import com.davidrandoll.automation.engine.http.events.AEHttpRequestEvent;
import com.davidrandoll.spring_web_captor.utils.HttpServletUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component("onHttpRequestTrigger")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "onHttpRequestTrigger", ignored = OnHttpRequestTrigger.class)
public class OnHttpRequestTrigger extends PluggableTrigger<OnHttpRequestTriggerContext> {
    private final ObjectMapper objectMapper;

    @Override
    public boolean isTriggered(EventContext ec, OnHttpRequestTriggerContext tc) {
        if (!(ec.getEvent() instanceof AEHttpRequestEvent event)) return false;

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

        return true;
    }
}