package com.automation.engine.http.publisher.response;

import com.automation.engine.core.AutomationEngine;
import com.automation.engine.http.publisher.HttpServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
//@Component
@RequiredArgsConstructor
public class CustomErrorAttribute extends DefaultErrorAttributes {
    private final AutomationEngine engine;

    /**
     * Capture the exception to be used in the {@link  HttpResponseEventPublisher#doFilterInternal}
     */
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        CachedBodyHttpServletResponse responseWrapper = HttpServletUtils.toCachedBodyHttpServletResponse(response);
        responseWrapper.setException(ex);
        return super.resolveException(request, response, handler, ex);
    }
}