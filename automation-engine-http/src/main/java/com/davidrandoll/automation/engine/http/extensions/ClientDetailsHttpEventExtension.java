package com.davidrandoll.automation.engine.http.extensions;

import com.davidrandoll.spring_web_captor.event.HttpRequestEvent;
import com.davidrandoll.spring_web_captor.event.HttpResponseEvent;
import com.davidrandoll.spring_web_captor.extensions.IHttpEventExtension;
import com.davidrandoll.spring_web_captor.utils.HttpServletUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * This class is responsible for extending HTTP events with client details such as IP address and user agent.
 * It implements the IHttpEventExtension interface to provide additional information for both request and response events.
 */
@Slf4j
@Component("clientDetailsHttpEventExtension")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "clientDetailsHttpEventExtension", ignored = {ClientDetailsHttpEventExtension.class})
public class ClientDetailsHttpEventExtension implements IHttpEventExtension {
    @Override
    public Map<String, Object> extendRequestEvent(HttpRequestEvent requestEvent) {
        return getClientDetails();
    }

    @Override
    public Map<String, Object> extendResponseEvent(HttpRequestEvent requestEvent, HttpResponseEvent responseEvent) {
        return getClientDetails();
    }

    private Map<String, Object> getClientDetails() {
        Map<String, Object> defaultResponse = Map.of(
                "user_ip", "Unknown",
                "user_agent", "Unknown"
        );
        try {
            var request = HttpServletUtils.getCurrentHttpRequest();
            if (request == null) return defaultResponse;

            var ip = HttpServletUtils.getClientIpAddressIfServletRequestExist();
            var userAgent = Optional.ofNullable(request.getHeader("User-Agent"))
                    .orElse("Unknown");
            return Map.of(
                    "user_ip", ip,
                    "user_agent", userAgent
            );
        } catch (Exception e) {
            log.error("Error getting client details", e);
            return defaultResponse;
        }
    }
}
