package com.automation.engine.http.publisher;

import com.automation.engine.http.publisher.request.CachedBodyHttpServletRequest;
import com.automation.engine.http.publisher.response.CachedBodyHttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@UtilityClass
public class HttpServletUtils {

    public static final String DEFAULT_IP = "0.0.0.0";

    public CachedBodyHttpServletRequest toCachedBodyHttpServletRequest(@NonNull HttpServletRequest request) throws IOException {
        if (request instanceof CachedBodyHttpServletRequest cachedBodyHttpServletRequest)
            return cachedBodyHttpServletRequest;
        return new CachedBodyHttpServletRequest(request);
    }

    public CachedBodyHttpServletResponse toCachedBodyHttpServletResponse(@NonNull HttpServletResponse response) {
        if (response instanceof CachedBodyHttpServletResponse cachedBodyHttpServletResponse)
            return cachedBodyHttpServletResponse;
        return new CachedBodyHttpServletResponse(response);
    }

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR",
            "X-Real-IP"
    };

    @NonNull
    public static String getClientIpAddressIfServletRequestExist() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) return DEFAULT_IP;

        String ip = null;
        HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
        for (String header : IP_HEADER_CANDIDATES) {
            String ipList = request.getHeader(header);
            if (ipList != null && !ipList.isEmpty() && !"unknown".equalsIgnoreCase(ipList)) {
                ip = ipList.split(",")[0];
                break;
            }
        }
        if (ip == null) ip = request.getRemoteAddr();
        if (ip != null && ip.equals("0:0:0:0:0:0:0:1")) ip = "127.0.0.1";

        return Optional.ofNullable(ip)
                .map(String::trim)
                .orElse(DEFAULT_IP);
    }
}