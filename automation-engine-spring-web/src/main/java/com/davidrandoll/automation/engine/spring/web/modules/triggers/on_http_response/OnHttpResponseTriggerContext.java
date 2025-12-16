package com.davidrandoll.automation.engine.spring.web.modules.triggers.on_http_response;

import com.davidrandoll.automation.engine.core.triggers.ITriggerContext;
import com.davidrandoll.spring_web_captor.event.HttpMethodEnum;
import com.davidrandoll.automation.engine.spring.web.jackson.flexible_httpstatus.FlexibleHttpStatusList;
import com.davidrandoll.automation.engine.spring.web.jackson.flexible_map_object.FlexibleMapObject;
import com.davidrandoll.automation.engine.spring.web.jackson.flexible_method.FlexibleHttpMethodList;
import com.davidrandoll.automation.engine.spring.web.jackson.flexible_multi_value_map.FlexibleMultiValueMap;
import com.davidrandoll.automation.engine.spring.web.jackson.flexible_string_list.FlexibleStringList;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        OnHttpResponseTriggerContext.Fields.alias,
        OnHttpResponseTriggerContext.Fields.description,
        OnHttpResponseTriggerContext.Fields.methods,
        OnHttpResponseTriggerContext.Fields.fullPaths,
        OnHttpResponseTriggerContext.Fields.paths,
        OnHttpResponseTriggerContext.Fields.headers,
        OnHttpResponseTriggerContext.Fields.queryParams,
        OnHttpResponseTriggerContext.Fields.pathParams,
        OnHttpResponseTriggerContext.Fields.requestBody,
        OnHttpResponseTriggerContext.Fields.responseBody,
        OnHttpResponseTriggerContext.Fields.responseStatuses,
        OnHttpResponseTriggerContext.Fields.errorDetail
})
public class OnHttpResponseTriggerContext implements ITriggerContext {
    /** Unique identifier for this trigger */
    private String alias;

    /** Human-readable description of what this trigger responds to */
    private String description;

    /** HTTP methods to match for the request (e.g., GET, POST, PUT, DELETE). If not specified, all methods will match */
    @JsonAlias({"method", "methods"})
    @FlexibleHttpMethodList
    private List<HttpMethodEnum> methods;

    /** Full URL paths to match for the request including context path (e.g., "/api/v1/users"). Supports wildcards */
    @JsonAlias({"fullPath", "fullPaths", "fullUrl", "fullUrls"})
    @FlexibleStringList
    private List<String> fullPaths;

    /** Request paths to match excluding context path (e.g., "/users"). Supports wildcards */
    @JsonAlias({"path", "paths"})
    @FlexibleStringList
    private List<String> paths;

    /** HTTP request headers that must be present for this trigger to activate */
    @JsonAlias({"headers", "header"})
    @FlexibleMultiValueMap
    private HttpHeaders headers;

    /** Query parameters that must be present in the request URL for this trigger to activate */
    @JsonAlias({"query", "queryString", "queryParam", "queryParams"})
    @FlexibleMultiValueMap
    private MultiValueMap<String, String> queryParams;

    /** Path parameters extracted from the URL pattern that must match for this trigger to activate */
    @JsonAlias({"pathParams", "pathParam"})
    @FlexibleMultiValueMap
    private MultiValueMap<String, String> pathParams;

    /** Request body content that must match for this trigger to activate. Can be a JSON object or any value */
    @JsonAlias({"body", "requestBody"})
    private JsonNode requestBody;

    /** Response body content that must match for this trigger to activate. Can be a JSON object or any value */
    @JsonAlias({"responseBody"})
    private JsonNode responseBody;

    /** HTTP response status codes to match (e.g., 200, 404, 500). Trigger activates only for these statuses */
    @JsonAlias({"status", "responseStatus"})
    @FlexibleHttpStatusList
    private List<HttpStatus> responseStatuses;

    /** Error details that must be present for this trigger to activate. Typically used for exception-based responses */
    @JsonAlias({"error", "errorDetail"})
    @FlexibleMapObject
    private Map<String, Object> errorDetail;

    public boolean hasMethods() {
        return methods != null && !methods.isEmpty();
    }

    public boolean hasFullPaths() {
        return fullPaths != null && !fullPaths.isEmpty();
    }

    public boolean hasPaths() {
        return paths != null && !paths.isEmpty();
    }

    public boolean hasHeaders() {
        return headers != null && !headers.isEmpty();
    }

    public boolean hasQueryParams() {
        return queryParams != null && !queryParams.isEmpty();
    }

    public boolean hasPathParams() {
        return pathParams != null && !pathParams.isEmpty();
    }

    public boolean hasRequestBody() {
        return requestBody != null && !requestBody.isEmpty();
    }

    public boolean hasResponseBody() {
        return responseBody != null && !responseBody.isEmpty();
    }

    public boolean hasResponseStatuses() {
        return responseStatuses != null && !responseStatuses.isEmpty();
    }

    public boolean hasErrorDetail() {
        return errorDetail != null && !errorDetail.isEmpty();
    }
}