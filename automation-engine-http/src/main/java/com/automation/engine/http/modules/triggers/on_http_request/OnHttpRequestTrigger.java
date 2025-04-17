package com.automation.engine.http.modules.triggers.on_http_request;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.http.utils.HttpServletUtils;
import com.automation.engine.http.utils.JsonNodeMatcher;
import com.automation.engine.spi.PluggableTrigger;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("onHttpRequestTrigger")
@RequiredArgsConstructor
public class OnHttpRequestTrigger extends PluggableTrigger<OnHttpRequestTriggerContext> {
    private final ObjectMapper objectMapper;

    @Override
    public boolean isTriggered(EventContext ec, OnHttpRequestTriggerContext tc) {
        if (!(ec.getEvent() instanceof HttpRequestEvent event)) return false;

        var isMethodTriggered = tc.hasMethods() && !tc.getMethods().contains(event.getMethod());
        if (isMethodTriggered) return false;

        var fullUrlParsed = HttpServletUtils.normalizedUrl(event.getFullUrl());
        var isFullUrlTriggered = tc.hasFullPaths() && tc.getFullPaths().stream().noneMatch(fullUrlParsed::matches);
        if (isFullUrlTriggered) return false;

        var pathParsed = HttpServletUtils.normalizedUrl(event.getPath());
        var isPathTriggered = tc.hasPaths() && tc.getPaths().stream().noneMatch(pathParsed::matches);
        if (isPathTriggered) return false;

        var isHeaderTriggered = tc.hasHeaders() && !JsonNodeMatcher.checkObject(tc.getHeaders(), event.getHeaders(), objectMapper);
        if (isHeaderTriggered) return false;

        var isQueryParamTriggered = tc.hasQueryParams() && !JsonNodeMatcher.checkObject(tc.getQueryParams(), event.getQueryParams(), objectMapper);
        if (isQueryParamTriggered) return false;

        var isPathParamTriggered = tc.hasPathParams() && !JsonNodeMatcher.checkObject(tc.getPathParams(), event.getPathParams(), objectMapper);
        if (isPathParamTriggered) return false;

        var isRequestBodyTriggered = tc.hasBody() && !JsonNodeMatcher.checkJsonNode(tc.getRequestBody(), event.getRequestBody());
        if (isRequestBodyTriggered) return false;

        return true;
    }
}