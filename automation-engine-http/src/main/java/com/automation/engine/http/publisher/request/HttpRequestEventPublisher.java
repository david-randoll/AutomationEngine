package com.automation.engine.http.publisher.request;

import com.automation.engine.core.AutomationEngine;
import com.automation.engine.http.event.HttpRequestEvent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpRequestEventPublisher implements HandlerInterceptor {
    private final AutomationEngine engine;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        CachedBodyHttpServletRequest requestWrapper = null;
        if (request instanceof CachedBodyHttpServletRequest cachedBodyHttpServletRequest) {
            requestWrapper = cachedBodyHttpServletRequest;
        } else {
            requestWrapper = new CachedBodyHttpServletRequest(request);
        }

        requestWrapper.setEndpointExists(true);
        HttpRequestEvent requestEvent = requestWrapper.toHttpRequestEvent();
        engine.publishEvent(requestEvent);

        return true;
    }
}