package com.automation.engine.http.modules.triggers.on_http_response;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.event.HttpResponseEvent;
import com.automation.engine.http.utils.HttpServletUtils;
import com.automation.engine.http.utils.JsonNodeMatcher;
import com.automation.engine.http.utils.MultiValueMatcher;
import com.automation.engine.spi.PluggableTrigger;
import org.springframework.stereotype.Component;

@Component("onHttpResponseTrigger")
public class OnHttpResponseTrigger extends PluggableTrigger<OnHttpResponseTriggerContext> {
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

        var isHeaderTriggered = tc.hasHeaders() && MultiValueMatcher.checkMap(tc.getHeaders(), event.getHeaders());
        if (isHeaderTriggered) return false;

        var isQueryParamTriggered = tc.hasQueryParams() && MultiValueMatcher.checkMap(tc.getQueryParams(), event.getQueryParams());
        if (isQueryParamTriggered) return false;

        var isPathParamTriggered = tc.hasPathParams() && MultiValueMatcher.checkMap(tc.getPathParams(), event.getPathParams());
        if (isPathParamTriggered) return false;

        var isRequestBodyTriggered = tc.hasRequestBody() && !JsonNodeMatcher.checkJsonNode(tc.getRequestBody(), event.getRequestBody());
        if (isRequestBodyTriggered) return false;

        var isResponseBodyTriggered = tc.hasResponseBody() && !JsonNodeMatcher.checkJsonNode(tc.getResponseBody(), event.getResponseBody());
        if (isResponseBodyTriggered) return false;

        var isResponseStatusTriggered = tc.hasResponseStatuses() && !tc.getResponseStatuses().contains(event.getResponseStatus());
        if (isResponseStatusTriggered) return false;

        var errorDetailTriggered = tc.hasErrorDetail() && MultiValueMatcher.checkObject(tc.getErrorDetail(), event.getErrorDetail());
        if (errorDetailTriggered) return false;

        return true;
    }
}