package com.automation.engine.http.publisher.request;

import com.automation.engine.core.AutomationEngine;
import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.http.event.IHttpEventExtension;
import com.automation.engine.http.publisher.HttpServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpRequestEventPublisher implements HandlerInterceptor {
    private final AutomationEngine engine;
    private final List<IHttpEventExtension> httpEventExtensions;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        CachedBodyHttpServletRequest requestWrapper = HttpServletUtils.toCachedBodyHttpServletRequest(request);
        requestWrapper.setEndpointExists(true);

        HttpRequestEvent requestEvent = requestWrapper.toHttpRequestEvent();
        publishRequestEvent(requestEvent);

        return true;
    }

    private void publishRequestEvent(HttpRequestEvent requestEvent) {
        if (requestEvent.getPath().equalsIgnoreCase("/error")) {
            return;
        }

        for (IHttpEventExtension extension : httpEventExtensions) {
            var additionalData = extension.extendRequestEvent(requestEvent);
            requestEvent.addAdditionalData(additionalData);
        }
        engine.publishEvent(requestEvent);
    }
}