package com.automation.engine.http.modules.triggers.on_http_request;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.spi.PluggableTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

@Component("onHttpRequestTrigger")
public class OnHttpRequestTrigger extends PluggableTrigger<OnHttpRequestTriggerContext> {
    @Override
    public boolean isTriggered(EventContext ec, OnHttpRequestTriggerContext tc) {
        if (!(ec.getEvent() instanceof HttpRequestEvent event)) return false;

        var isMethodTriggered = tc.hasMethods() && !tc.getMethods().contains(event.getMethod());
        if (isMethodTriggered) return false;

        var fullUrlParsed = normalizedUrl(event.getFullUrl());
        var isFullUrlTriggered = tc.hasFullPaths() && tc.getFullPaths().stream().noneMatch(fullUrlParsed::matches);
        if (isFullUrlTriggered) return false;

        var pathParsed = normalizedUrl(event.getPath());
        var isPathTriggered = tc.hasPaths() && tc.getPaths().stream().noneMatch(pathParsed::matches);
        if (isPathTriggered) return false;

        var isHeaderTriggered = tc.hasHeaders() && checkMap(tc.getHeaders(), event.getHeaders());
        if (isHeaderTriggered) return false;

        var isQueryParamTriggered = tc.hasQueryParams() && checkMap(tc.getQueryParams(), event.getQueryParams());
        if (isQueryParamTriggered) return false;

        var isPathParamTriggered = tc.hasPathParams() && checkMap(tc.getPathParams(), event.getPathParams());
        if (isPathParamTriggered) return false;

        return true;
    }

    private static String normalizedUrl(String url) {
        if (url == null) return null;
        return url.replaceAll("/$", "");
    }

    private static boolean checkMap(MultiValueMap<String, String> queryParams, Map<String, String> eventQueryParams) {
        LinkedMultiValueMap<String, String> eventMultiValueMap = eventQueryParams.entrySet().stream()
                .collect(LinkedMultiValueMap::new, (map, entry) -> map.add(entry.getKey(), entry.getValue()), LinkedMultiValueMap::addAll);
        return checkMap(queryParams, eventMultiValueMap);
    }

    private static boolean checkMap(MultiValueMap<String, String> queryParams, MultiValueMap<String, String> eventQueryParams) {
        for (var queryParam : queryParams.entrySet()) {
            String queryParamName = queryParam.getKey();
            List<String> queryParamValue = queryParam.getValue();
            var queryParamValueList = eventQueryParams.getOrDefault(queryParamName, List.of());
            if (queryParamValueList == null || queryParamValueList.isEmpty()) return true;
            if (queryParamValue.stream().noneMatch(queryParamValueList::contains)) return true;
        }
        return false;
    }
}
