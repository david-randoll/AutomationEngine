package com.davidrandoll.automation.engine.http.utils;

import com.davidrandoll.automation.engine.http.exceptions.AutomationEngineHttpParseException;
import com.davidrandoll.automation.engine.http.publisher.request.CachedBodyHttpServletRequest;
import com.davidrandoll.automation.engine.http.publisher.response.CachedBodyHttpServletResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@UtilityClass
public class HttpServletUtils {
    public static final String DEFAULT_IP = "0.0.0.0";

    public CachedBodyHttpServletRequest toCachedBodyHttpServletRequest(@NonNull HttpServletRequest request, ObjectMapper mapper) {
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
        HttpServletRequest request = getCurrentHttpRequest();
        if (request == null) return DEFAULT_IP;
        String ip = null;
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

    @Nullable
    public static HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) return null;

        return ((ServletRequestAttributes) attributes).getRequest();
    }

    public static String normalizedUrl(String url) {
        if (url == null) return null;
        return url.replaceAll("/$", "");
    }

    public static JsonNode parseByteArrayToJsonNode(String contentType, byte[] cachedBody, ObjectMapper objectMapper) {
        try {
            JsonNodeFactory factory = JsonNodeFactory.instance;
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

    public static JsonNode toJsonNode(String contentType, Object body, ObjectMapper objectMapper) {
        var factory = JsonNodeFactory.instance;
        if (body == null) return factory.nullNode();
        try {
            if (contentType != null && contentType.contains("json")) {
                return objectMapper.readTree(body.toString());
            } else {
                return factory.textNode(body.toString());
            }
        } catch (IOException e) {
            throw new AutomationEngineHttpParseException(e);
        }
    }
}