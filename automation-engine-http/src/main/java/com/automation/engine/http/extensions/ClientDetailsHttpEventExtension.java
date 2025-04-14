package com.automation.engine.http.extensions;

import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.http.event.HttpResponseEvent;
import com.automation.engine.http.utils.HttpServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClientDetailsHttpEventExtension implements IHttpEventExtension {
    private final HttpServletRequest request;

    @Override
    public Map<String, Object> extendRequestEvent(HttpRequestEvent requestEvent) {
        return getClientDetails();
    }

    @Override
    public Map<String, Object> extendResponseEvent(HttpResponseEvent responseEvent) {
        return getClientDetails();
    }

    private Map<String, Object> getClientDetails() {
        var ip = HttpServletUtils.getClientIpAddressIfServletRequestExist();
        var userAgent = Optional.ofNullable(request.getHeader("User-Agent"))
                .orElse("Unknown");
        return Map.of(
                "user_ip", ip,
                "user_agent", userAgent
        );
    }
}
