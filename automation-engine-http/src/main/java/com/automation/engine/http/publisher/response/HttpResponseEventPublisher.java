package com.automation.engine.http.publisher.response;

import com.automation.engine.core.AutomationEngine;
import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.http.event.HttpResponseEvent;
import com.automation.engine.http.publisher.request.CachedBodyHttpServletRequest;
import com.automation.engine.http.publisher.request.HttpRequestEventPublisher;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletionStage;


@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpResponseEventPublisher extends OncePerRequestFilter {
    private final AutomationEngine engine;
    private final DefaultErrorAttributes defaultErrorAttributes;

    /**
     * NOTE: Cannot publish the request event here because the path params are not available here yet.
     * After the filter chain is executed, the path params are available in the requestWrapper object.
     * This is why in the {@link  HttpRequestEventPublisher#preHandle}, the event is published in the preHandle method.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        var requestWrapper = new CachedBodyHttpServletRequest(request);
        var responseWrapper = new CachedBodyHttpServletResponse(response);

        filterChain.doFilter(requestWrapper, responseWrapper);

        if (!requestWrapper.isEndpointExists()) return;

        HttpStatus responseStatus = HttpStatus.valueOf(responseWrapper.getStatus());
        HttpRequestEvent requestEvent = requestWrapper.toHttpRequestEvent();
        HttpResponseEvent responseEvent = toHttpResponseEvent(requestEvent, responseStatus);

        if (responseStatus.is2xxSuccessful()) {
            CompletionStage<String> responseBody = responseWrapper.getResponseBody(requestWrapper);

            responseBody.thenAccept(body -> {
                responseEvent.setResponseBody(body);
                engine.publishEvent(responseEvent);
            });
        } else {
            var errorAttributes = getErrorAttributes(request);
            responseEvent.addErrorDetail(errorAttributes);
            engine.publishEvent(responseEvent);
        }
    }

    private static HttpResponseEvent toHttpResponseEvent(HttpRequestEvent requestEvent, HttpStatus responseStatus) {
        return HttpResponseEvent.builder()
                .fullUrl(requestEvent.getFullUrl())
                .path(requestEvent.getPath())
                .method(requestEvent.getMethod())
                .headers(requestEvent.getHeaders())
                .queryParams(requestEvent.getQueryParams())
                .pathParams(requestEvent.getPathParams())
                .requestBody(requestEvent.getRequestBody())
                .responseBody("")
                .responseStatus(responseStatus)
                .build();
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request) {
        WebRequest webRequest = new ServletWebRequest(request);
        var options = ErrorAttributeOptions.of(ErrorAttributeOptions.Include.values());
        return defaultErrorAttributes.getErrorAttributes(webRequest, options);
    }
}