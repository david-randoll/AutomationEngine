package com.davidrandoll.automation.engine.spring.web.utils;

import com.davidrandoll.automation.engine.spring.web.events.AEHttpRequestEvent;
import com.davidrandoll.spring_web_captor.field_captor.registry.IFieldCaptorRegistry;
import com.davidrandoll.spring_web_captor.publisher.request.CachedBodyHttpServletRequest;
import com.davidrandoll.spring_web_captor.utils.HttpServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HttpServletRequestConverter {
    private final IFieldCaptorRegistry fieldCaptorRegistry;

    public AEHttpRequestEvent toHttpEvent(HttpServletRequest request) {
        CachedBodyHttpServletRequest requestWrapper = HttpServletUtils.toCachedBodyHttpServletRequest(request);
        return new AEHttpRequestEvent(requestWrapper.toHttpRequestEvent(fieldCaptorRegistry));
    }
}