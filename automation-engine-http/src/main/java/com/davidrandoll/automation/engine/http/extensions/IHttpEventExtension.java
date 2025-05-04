package com.davidrandoll.automation.engine.http.extensions;

import com.davidrandoll.automation.engine.http.event.HttpRequestEvent;
import com.davidrandoll.automation.engine.http.event.HttpResponseEvent;

import java.util.Map;

public interface IHttpEventExtension {
    /**
     * This method is called before the {@link HttpRequestEvent} is published.
     * It can be used to add additional data ({@link HttpRequestEvent#setAdditionalData}) to the request event.
     * <p>
     * For example, anything from spring security (username, roles, etc). Or tenantId if in a multi-tenant environment.
     * </p>
     */
    default Map<String, Object> extendRequestEvent(HttpRequestEvent requestEvent) {
        return Map.of();
    }

    /**
     * This method is called before the {@link HttpResponseEvent} is published.
     * It can be used to add additional data ({@link HttpResponseEvent#setAdditionalData}) to the response event.
     * <p>
     * For example, anything from spring security (username, roles, etc). Or tenantId if in a multi-tenant environment.
     * </p>
     */
    default Map<String, Object> extendResponseEvent(HttpRequestEvent requestEvent, HttpResponseEvent responseEvent) {
        return Map.of();
    }
}