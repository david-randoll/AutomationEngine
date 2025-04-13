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
        if (tc.hasMethods()) {
            if (!tc.getMethods().contains(event.getMethod())) return false;
        }

        if (tc.hasFullPaths()) {
            var fullPaths = tc.getFullPathsAsRegex();
            if (fullPaths.stream().noneMatch(event.getFullUrl()::matches)) return false;
        }

        if (tc.hasPaths()) {
            var paths = tc.getPathsAsRegex();
            if (paths.stream().noneMatch(event.getPath()::matches)) return false;
        }

        if (tc.hasHeaders()) {
            if (checkMap(tc.getHeaders(), event.getHeaders())) return false;
        }

        if (tc.hasQueryParams()) {
            if (checkMap(tc.getQueryParams(), event.getQueryParams())) return false;
        }

        if (tc.hasPathParams()) {
            if (checkMap(tc.getPathParams(), event.getPathParams())) return false;
        }

        return true;
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
            if (queryParamValueList == null || queryParamValueList.isEmpty()) return false;
            if (queryParamValue.stream().noneMatch(queryParamValueList::contains)) return false;
        }
        return false;
    }
}
