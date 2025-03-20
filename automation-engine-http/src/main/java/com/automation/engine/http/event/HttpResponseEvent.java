package com.automation.engine.http.event;

import com.automation.engine.core.events.Event;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Map;

public class HttpResponseEvent extends Event {
    public static final String EVENT_NAME = HttpResponseEvent.class.getSimpleName();

    public static final String FULL_URL = "fullUrl";
    public static final String PATH = "path";
    public static final String METHOD = "method";
    public static final String HEADERS = "headers";
    public static final String QUERY_PARAMS = "queryParams";
    public static final String PATH_PARAMS = "pathParams";
    public static final String REQUEST_BODY = "requestBody";
    public static final String RESPONSE_BODY = "responseBody";
    public static final String RESPONSE_STATUS = "responseStatus";


    public HttpResponseEvent(String fullUrl,
                             String path,
                             HttpMethodEnum method,
                             Map<String, ArrayList<String>> headers,
                             Map<String, Object> queryParams,
                             Map<String, Object> pathParams,
                             String requestBody,
                             String responseBody,
                             HttpStatus responseStatus) {
        super(EVENT_NAME, Map.of(
                FULL_URL, fullUrl,
                PATH, path,
                METHOD, method,
                HEADERS, headers,
                QUERY_PARAMS, queryParams,
                PATH_PARAMS, pathParams,
                REQUEST_BODY, requestBody,
                RESPONSE_BODY, responseBody,
                RESPONSE_STATUS, responseStatus
        ));
    }

    public String getFullUrl() {
        return super.getData().get(FULL_URL).toString();
    }

    public String getPath() {
        return super.getData().get(PATH).toString();
    }

    public HttpMethodEnum getMethod() {
        return (HttpMethodEnum) super.getData().get(METHOD);
    }

    public Map<String, ArrayList<String>> getHeaders() {
        return (Map<String, ArrayList<String>>) super.getData().get(HEADERS);
    }

    public Map<String, Object> getQueryParams() {
        return (Map<String, Object>) super.getData().get(QUERY_PARAMS);
    }

    public Map<String, Object> getPathParams() {
        return (Map<String, Object>) super.getData().get(PATH_PARAMS);
    }

    public String getRequestBody() {
        return (String) super.getData().get(REQUEST_BODY);
    }

    public String getResponseBody() {
        return (String) super.getData().get(RESPONSE_BODY);
    }

    public HttpStatus getResponseStatus() {
        return (HttpStatus) super.getData().get(RESPONSE_STATUS);
    }
}