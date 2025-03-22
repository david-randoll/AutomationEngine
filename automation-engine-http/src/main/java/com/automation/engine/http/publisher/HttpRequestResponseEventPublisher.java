package com.automation.engine.http.publisher;

import com.automation.engine.core.AutomationEngine;
import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.http.event.HttpResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpRequestResponseEventPublisher extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;
    private final AutomationEngine engine;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        var requestWrapper = new CachedBodyHttpServletRequest(request);
        var responseWrapper = new CachedBodyHttpServletResponse(response);

        if (ObjectUtils.isEmpty(requestWrapper)) {
            filterChain.doFilter(request, response);
        } else {
            filterChain.doFilter(requestWrapper, responseWrapper);
        }

        HttpRequestEvent requestEvent = requestWrapper.toHttpRequestEvent();
        engine.publishEvent(requestEvent);

        HttpStatus responseStatus = HttpStatus.valueOf(responseWrapper.getStatus());
        CompletionStage<String> responseBody = responseWrapper.getResponseBody(requestWrapper);

        responseBody.thenAccept(body -> {
            var responseEvent = new HttpResponseEvent(
                    requestEvent.getFullUrl(),
                    requestEvent.getPath(),
                    requestEvent.getMethod(),
                    requestEvent.getHeaders(),
                    requestEvent.getQueryParams(),
                    requestEvent.getPathParams(),
                    requestEvent.getRequestBody(),
                    body,
                    responseStatus
            );
            engine.publishEvent(responseEvent);
        });
    }
}