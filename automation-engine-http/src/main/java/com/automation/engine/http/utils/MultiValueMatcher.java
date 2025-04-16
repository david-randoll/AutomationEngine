package com.automation.engine.http.utils;

import lombok.experimental.UtilityClass;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@UtilityClass
public class MultiValueMatcher {
    public static boolean checkMapObject(MultiValueMap<String, String> queryParams, Map<String, Object> eventQueryParams) {
        if (queryParams == null || eventQueryParams == null) return true;
        LinkedMultiValueMap<String, String> eventMultiValueMap = eventQueryParams.entrySet().stream()
                .collect(LinkedMultiValueMap::new, (map, entry) -> map.add(entry.getKey(), entry.getValue().toString()), LinkedMultiValueMap::addAll);
        return checkMap(queryParams, eventMultiValueMap);
    }

    public static boolean checkMap(MultiValueMap<String, String> queryParams, Map<String, String> eventQueryParams) {
        if (queryParams == null || eventQueryParams == null) return true;
        LinkedMultiValueMap<String, String> eventMultiValueMap = eventQueryParams.entrySet().stream()
                .collect(LinkedMultiValueMap::new, (map, entry) -> map.add(entry.getKey(), entry.getValue()), LinkedMultiValueMap::addAll);
        return checkMap(queryParams, eventMultiValueMap);
    }

    public static boolean checkMap(MultiValueMap<String, String> multiValueMap, MultiValueMap<String, String> eventMultiValueMap) {
        if (multiValueMap == null || eventMultiValueMap == null) return true;

        // Build a lowercased map of event query params for case-insensitive key matching
        Map<String, List<String>> normalizedEventQueryParams = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : eventMultiValueMap.entrySet()) {
            String lowerKey = entry.getKey().trim().toLowerCase();
            List<String> values = entry.getValue().stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .toList();
            normalizedEventQueryParams.put(lowerKey, values);
        }

        for (var entry : multiValueMap.entrySet()) {
            String expectedKey = entry.getKey().trim().toLowerCase();
            List<String> expectedValues = entry.getValue().stream()
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

    public static boolean checkObject(Object expected, Object actual) {
        if (expected == null) {
            return true; // null expected = wildcard
        }
        if (actual == null) {
            return false;
        }

        if (expected instanceof Map<?, ?> expectedMap) {
            if (!(actual instanceof Map<?, ?> actualMap)) return false;

            for (Map.Entry<?, ?> entry : expectedMap.entrySet()) {
                String key = entry.getKey().toString();
                Object expectedValue = entry.getValue();
                Object actualValue = actualMap.get(key);
                if (checkObject(expectedValue, actualValue)) {
                    return false;
                }
            }
            return true;
        }

        if (expected instanceof List<?> expectedList) {
            if (!(actual instanceof List<?> actualList)) {
                // actual is a single value, check if any expected element matches it
                for (Object expectedElement : expectedList) {
                    if (checkObject(expectedElement, actual)) {
                        return false;
                    }
                }
                return true;
            }

            for (Object expectedElement : expectedList) {
                boolean matched = true;
                for (Object actualElement : actualList) {
                    if (checkObject(expectedElement, actualElement)) {
                        matched = false;
                        break;
                    }
                }
                if (matched) return true;
            }
            return true;
        }

        String expectedText = expected.toString();
        String actualText = actual.toString();
        var pattern = Pattern.compile(expectedText);
        return pattern.matcher(actualText).matches();
    }
}