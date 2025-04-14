package com.automation.engine.http.utils;

import com.automation.engine.http.extensions.AutomationEngineHttpParseException;
import com.automation.engine.http.publisher.request.CachedBodyHttpServletRequest;
import com.automation.engine.http.publisher.response.CachedBodyHttpServletResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@UtilityClass
public class HttpServletUtils {
    public static final String DEFAULT_IP = "0.0.0.0";

    public CachedBodyHttpServletRequest toCachedBodyHttpServletRequest(@NonNull HttpServletRequest request, ObjectMapper mapper) throws IOException {
        if (request instanceof CachedBodyHttpServletRequest cachedBodyHttpServletRequest)
            return cachedBodyHttpServletRequest;
        return new CachedBodyHttpServletRequest(request, mapper);
    }

    public CachedBodyHttpServletResponse toCachedBodyHttpServletResponse(@NonNull HttpServletResponse response, ObjectMapper mapper) {
        if (response instanceof CachedBodyHttpServletResponse cachedBodyHttpServletResponse)
            return cachedBodyHttpServletResponse;
        return new CachedBodyHttpServletResponse(response, mapper);
    }

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR",
            "X-Real-IP"
    };

    @NonNull
    public static String getClientIpAddressIfServletRequestExist() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) return DEFAULT_IP;

        String ip = null;
        HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
        for (String header : IP_HEADER_CANDIDATES) {
            String ipList = request.getHeader(header);
            if (ipList != null && !ipList.isEmpty() && !"unknown".equalsIgnoreCase(ipList)) {
                ip = ipList.split(",")[0];
                break;
            }
        }
        if (ip == null) ip = request.getRemoteAddr();
        if (ip != null && ip.equals("0:0:0:0:0:0:0:1")) ip = "127.0.0.1";

        return Optional.ofNullable(ip)
                .map(String::trim)
                .orElse(DEFAULT_IP);
    }

    public static String normalizedUrl(String url) {
        if (url == null) return null;
        return url.replaceAll("/$", "");
    }

    public static boolean checkMap(MultiValueMap<String, String> queryParams, Map<String, String> eventQueryParams) {
        if (queryParams == null || eventQueryParams == null) return true;
        LinkedMultiValueMap<String, String> eventMultiValueMap = eventQueryParams.entrySet().stream()
                .collect(LinkedMultiValueMap::new, (map, entry) -> map.add(entry.getKey(), entry.getValue()), LinkedMultiValueMap::addAll);
        return checkMap(queryParams, eventMultiValueMap);
    }

    public static boolean checkMap(MultiValueMap<String, String> queryParams, MultiValueMap<String, String> eventQueryParams) {
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

    public static JsonNode parseByteArrayToJsonNode(String contentType, byte[] cachedBody, ObjectMapper objectMapper) {
        try {
            JsonNodeFactory factory = objectMapper.getNodeFactory();
            if (ObjectUtils.isEmpty(cachedBody)) return factory.nullNode();

            if (contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
                return objectMapper.readTree(cachedBody);
            } else {
                // if the content type is not JSON, we can try to parse it as a text node
                var stringBody = new String(cachedBody, StandardCharsets.UTF_8);
                return factory.textNode(stringBody);
            }
        } catch (IOException e) {
            throw new AutomationEngineHttpParseException(e);
        }
    }
}