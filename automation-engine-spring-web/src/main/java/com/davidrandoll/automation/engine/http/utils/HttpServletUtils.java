package com.davidrandoll.automation.engine.http.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@UtilityClass
public class HttpServletUtils {
    public static String normalizedUrl(String url) {
        if (url == null) return null;
        return url.replaceAll("/$", "");
    }

    public static JsonNode toJsonNode(String contentType, Object body, ObjectMapper objectMapper) {
        var factory = JsonNodeFactory.instance;
        if (body == null) return factory.nullNode();
        try {
            if (contentType != null && contentType.contains("json")) {
                return objectMapper.readTree(body.toString());
            }
        } catch (IOException e) {
            log.error("Failed to parse body as JSON: {}", body, e);
        }
        return factory.textNode(body.toString());
    }
}
