package com.automation.engine.modules.http_request.event;

import com.automation.engine.core.AutomationEngine;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpRequestResponseEventPublisher extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;
    private final AutomationEngine engine;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        var requestWrapper = new ContentCachingRequestWrapper(request);
        var responseWrapper = new ContentCachingResponseWrapper(response);

        String path = requestWrapper.getRequestURI();
        String fullUrl = requestWrapper.getRequestURL().toString();
        HttpMethodEnum method = HttpMethodEnum.valueOf(requestWrapper.getMethod());
        Map<String, ArrayList<String>> headers = RequestUtils.getHeaders(requestWrapper);
        Map<String, Object> queryParams = RequestUtils.getRequestParams(requestWrapper);
        Map<String, Object> pathParams = RequestUtils.getPathVariables(requestWrapper);
        JsonNode requestBody = RequestUtils.getRequestBody(requestWrapper, objectMapper);

        var requestEvent = new HttpRequestEvent(
                fullUrl,
                path,
                method,
                headers,
                queryParams,
                pathParams,
                requestBody
        );
        engine.publishEvent(requestEvent);

        if (ObjectUtils.isEmpty(requestWrapper)) {
            filterChain.doFilter(request, response);
        } else {
            filterChain.doFilter(requestWrapper, responseWrapper);
        }
        var responseStatus = responseWrapper.getStatus();
        var responseBody = RequestUtils.getResponseBody(requestWrapper, responseWrapper, objectMapper);
    }
}