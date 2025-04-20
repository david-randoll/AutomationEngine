package com.automation.engine.http.publisher.response;

import com.automation.engine.core.AutomationEngine;
import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.http.event.HttpResponseEvent;
import com.automation.engine.http.extensions.IHttpEventExtension;
import com.automation.engine.http.publisher.request.CachedBodyHttpServletRequest;
import com.automation.engine.http.publisher.request.HttpRequestEventPublisher;
import com.automation.engine.http.utils.HttpServletUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;


@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class HttpResponseEventPublisher extends OncePerRequestFilter {
    private final AutomationEngine engine;
    private final DefaultErrorAttributes defaultErrorAttributes;
    private final List<IHttpEventExtension> httpEventExtensions;
    private final ObjectMapper objectMapper;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver handlerExceptionResolver;

    /**
     * NOTE: Cannot publish the request event here because the path params are not available here yet.
     * After the filter chain is executed, the path params are available in the requestWrapper object.
     * This is why in the {@link  HttpRequestEventPublisher#preHandle}, the event is published in the preHandle method.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        CachedBodyHttpServletRequest requestWrapper = HttpServletUtils.toCachedBodyHttpServletRequest(request, objectMapper);
        CachedBodyHttpServletResponse responseWrapper = HttpServletUtils.toCachedBodyHttpServletResponse(response, objectMapper);

        filterChain.doFilter(requestWrapper, responseWrapper);

        try {
            buildAndPublishResponseEvent(request, requestWrapper, responseWrapper);
        } catch (Exception ex) {
            responseWrapper.resetBuffer();
            responseWrapper.setContentType("application/json;charset=UTF-8");
            handlerExceptionResolver.resolveException(requestWrapper, responseWrapper, null, ex);
        } finally {
            responseWrapper.copyBodyToResponse(); // IMPORTANT: copy response back into original response
        }
    }

    private void buildAndPublishResponseEvent(HttpServletRequest request, CachedBodyHttpServletRequest requestWrapper, CachedBodyHttpServletResponse responseWrapper) throws IOException {
        if (!requestWrapper.isEndpointExists()) return;

        final HttpStatus responseStatus = HttpStatus.valueOf(responseWrapper.getStatus());
        final HttpRequestEvent requestEvent = requestWrapper.toHttpRequestEvent();
        final HttpResponseEvent responseEvent = toHttpResponseEvent(requestEvent, responseStatus);

        if (responseStatus.is2xxSuccessful()) {
            CompletionStage<JsonNode> responseBody = responseWrapper.getResponseBody(requestWrapper);

            responseBody.thenAccept(body -> {
                responseEvent.setResponseBody(body);
                publishResponseEvent(requestEvent, responseEvent);
            });
        } else {
            var errorAttributes = getErrorAttributes(request);
            responseEvent.addErrorDetail(errorAttributes);
            publishResponseEvent(requestEvent, responseEvent);
        }
    }

    private void publishResponseEvent(HttpRequestEvent requestEvent, HttpResponseEvent responseEvent) {
        for (IHttpEventExtension extension : httpEventExtensions) {
            var additionalData = extension.extendResponseEvent(requestEvent, responseEvent);
            responseEvent.addAdditionalData(additionalData);
        }
        engine.publishEvent(responseEvent);
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
                .responseStatus(responseStatus)
                .build();
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request) {
        WebRequest webRequest = new ServletWebRequest(request);
        ErrorAttributeOptions options = ErrorAttributeOptions.of(ErrorAttributeOptions.Include.values());
        return defaultErrorAttributes.getErrorAttributes(webRequest, options);
    }
}