package com.automation.engine.http.modules.triggers.on_http_request;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.spi.PluggableTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        if (queryParams == null || eventQueryParams == null) return true;
        LinkedMultiValueMap<String, String> eventMultiValueMap = eventQueryParams.entrySet().stream()
                .collect(LinkedMultiValueMap::new, (map, entry) -> map.add(entry.getKey(), entry.getValue()), LinkedMultiValueMap::addAll);
        return checkMap(queryParams, eventMultiValueMap);
    }

    private static boolean checkMap(MultiValueMap<String, String> queryParams, MultiValueMap<String, String> eventQueryParams) {
        if (queryParams == null || eventQueryParams == null) return true;

        // Build a lowercased map of event query params for case-insensitive key matching
        Map<String, List<String>> normalizedEventQueryParams = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : eventQueryParams.entrySet()) {
            String lowerKey = entry.getKey().trim().toLowerCase();
            List<String> values = entry.getValue().stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .toList();
            normalizedEventQueryParams.put(lowerKey, values);
        }

        for (var queryParam : queryParams.entrySet()) {
            String expectedKey = queryParam.getKey().trim().toLowerCase();
            List<String> expectedValues = queryParam.getValue().stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .toList();

            List<String> actualValues = normalizedEventQueryParams.getOrDefault(expectedKey, List.of());
            if (actualValues == null || actualValues.isEmpty()) return true;

            boolean noneMatched = expectedValues.stream().noneMatch(expectedPattern ->
                    actualValues.stream()
                            .anyMatch(actualValue -> actualValue.matches("(?i)" + expectedPattern))
            );

            if (noneMatched) return true;
        }

        return false;
    }
}