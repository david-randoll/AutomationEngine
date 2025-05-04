package com.davidrandoll.automation.engine.http.extensions;

import com.davidrandoll.automation.engine.http.event.HttpRequestEvent;
import com.davidrandoll.automation.engine.http.event.HttpResponseEvent;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component("httpDurationFilterExtension")
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnMissingBean(name = "httpDurationFilterExtension")
public class HttpDurationFilterExtension extends OncePerRequestFilter implements IHttpEventExtension {
    private final ThreadLocal<Long> timeStartLocal = new InheritableThreadLocal<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        timeStartLocal.set(System.currentTimeMillis());
        filterChain.doFilter(request, response);
        timeStartLocal.remove();
    }

    @Override
    public Map<String, Object> extendResponseEvent(HttpRequestEvent requestEvent, HttpResponseEvent responseEvent) {
        var result = new HashMap<String, Object>();
        var timeStart = timeStartLocal.get();
        Duration duration = Duration.ofMillis(System.currentTimeMillis() - timeStart);
        result.put("duration", duration);
        return result;
    }
}