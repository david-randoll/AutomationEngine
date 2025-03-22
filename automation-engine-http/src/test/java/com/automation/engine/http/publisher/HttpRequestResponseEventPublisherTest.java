package com.automation.engine.http.publisher;

import com.automation.engine.http.AutomationEngineHttpApplication;
import com.automation.engine.http.event.HttpMethodEnum;
import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.http.event.HttpResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AutomationEngineHttpApplication.class)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HttpRequestResponseEventPublisherTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventCaptureListener eventCaptureListener; // Capture published events

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        eventCaptureListener.clearEvents(); // Reset events before each test
    }

    @Test
    void testGetRequestPublishesEvents() throws Exception {
        mockMvc.perform(get("/test/get"))
                .andExpect(status().isOk())
                .andExpect(content().string("GET response"));

        // Verify request event
        assertThat(eventCaptureListener.getRequestEvents()).hasSize(1);
        HttpRequestEvent requestEvent = eventCaptureListener.getRequestEvents().getFirst();
        assertThat(requestEvent.getPath()).isEqualTo("/test/get");
        assertThat(requestEvent.getMethod()).isEqualTo(HttpMethodEnum.GET);
        // queryParams
        assertThat(requestEvent.getQueryParams()).isEmpty();
        // pathParams
        assertThat(requestEvent.getPathParams()).isEmpty();
        // requestBody
        assertThat(requestEvent.getRequestBody()).isEmpty();
        // headers
        assertThat(requestEvent.getHeaders()).isEmpty();

        // Verify response event
        assertThat(eventCaptureListener.getResponseEvents()).hasSize(1);
        HttpResponseEvent responseEvent = eventCaptureListener.getResponseEvents().getFirst();
        assertThat(responseEvent.getResponseBody()).isEqualTo("GET response");
    }

    @Test
    void testPostRequestPublishesEvents() throws Exception {
        String requestBody = "{\"key\": \"value\"}";

        mockMvc.perform(post("/test/post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("value"));

        // Verify request event
        assertThat(eventCaptureListener.getRequestEvents()).hasSize(1);
        HttpRequestEvent requestEvent = eventCaptureListener.getRequestEvents().getFirst();
        assertThat(requestEvent.getPath()).isEqualTo("/test/post");
        assertThat(requestEvent.getMethod()).isEqualTo(HttpMethodEnum.POST);
        // requestBody
        var bodyJson = objectMapper.readTree(requestEvent.getRequestBody());
        assertThat(bodyJson.get("key").asText()).isEqualTo("value");
        // queryParams
        assertThat(requestEvent.getQueryParams()).isEmpty();
        // pathParams
        assertThat(requestEvent.getPathParams()).isEmpty();
        // headers contains content-type
        String contentType = Objects.requireNonNull(requestEvent.getHeaders().getContentType()).toString();
        String expectedContentType = MediaType.APPLICATION_JSON_VALUE;
        assertThat(contentType).startsWith(expectedContentType);

        // Verify response event
        assertThat(eventCaptureListener.getResponseEvents()).hasSize(1);
        HttpResponseEvent responseEvent = eventCaptureListener.getResponseEvents().getFirst();
        var responseBodyJson = objectMapper.readTree(responseEvent.getResponseBody());
        assertThat(responseBodyJson.get("key").asText()).isEqualTo("value");
    }

    @Test
    void testPutRequestPublishesEvents() throws Exception {
        String requestBody = "Updated content";

        mockMvc.perform(put("/test/put")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("Received: Updated content"));

        // Verify request event
        assertThat(eventCaptureListener.getRequestEvents()).hasSize(1);
        HttpRequestEvent requestEvent = eventCaptureListener.getRequestEvents().getFirst();
        assertThat(requestEvent.getPath()).isEqualTo("/test/put");
        assertThat(requestEvent.getMethod()).isEqualTo(HttpMethodEnum.PUT);
        // requestBody
        assertThat(requestEvent.getRequestBody()).isEqualTo("Updated content");
        // queryParams
        assertThat(requestEvent.getQueryParams()).isEmpty();
        // pathParams
        assertThat(requestEvent.getPathParams()).isEmpty();
        // headers contains content-type
        String contentType = Objects.requireNonNull(requestEvent.getHeaders().getContentType()).toString();
        String expectedContentType = MediaType.TEXT_PLAIN_VALUE;
        assertThat(contentType).startsWith(expectedContentType);

        // Verify response event
        assertThat(eventCaptureListener.getResponseEvents()).hasSize(1);
        HttpResponseEvent responseEvent = eventCaptureListener.getResponseEvents().getFirst();
        assertThat(responseEvent.getResponseBody()).isEqualTo("Received: Updated content");
    }

    @Test
    void testDeleteRequestPublishesEvents() throws Exception {
        mockMvc.perform(delete("/test/delete"))
                .andExpect(status().isNoContent());

        // Verify request event
        assertThat(eventCaptureListener.getRequestEvents()).hasSize(1);
        HttpRequestEvent requestEvent = eventCaptureListener.getRequestEvents().getFirst();
        assertThat(requestEvent.getPath()).isEqualTo("/test/delete");
        assertThat(requestEvent.getMethod()).isEqualTo(HttpMethodEnum.DELETE);
        // queryParams
        assertThat(requestEvent.getQueryParams()).isEmpty();
        // pathParams
        assertThat(requestEvent.getPathParams()).isEmpty();
        // headers
        assertThat(requestEvent.getHeaders()).isEmpty();
        // requestBody
        assertThat(requestEvent.getRequestBody()).isEmpty();

        // Verify response event
        assertThat(eventCaptureListener.getResponseEvents()).hasSize(1);
        HttpResponseEvent responseEvent = eventCaptureListener.getResponseEvents().getFirst();
        assertThat(responseEvent.getResponseStatus()).isEqualTo(HttpStatus.NO_CONTENT);
        // responseBody
        assertThat(responseEvent.getResponseBody()).isEmpty();
        // headers
        assertThat(responseEvent.getHeaders()).isEmpty();
        // requestBody
        assertThat(responseEvent.getRequestBody()).isEmpty();
        // queryParams
        assertThat(responseEvent.getQueryParams()).isEmpty();
        // pathParams
        assertThat(responseEvent.getPathParams()).isEmpty();
    }

    @Test
    void testGetWithQueryParamsPublishesEvents() throws Exception {
        mockMvc.perform(get("/test/get-with-params")
                        .param("key", "value")
                        .param("otherKey", "otherValue")
                        .header("Custom-Header", "HeaderValue"))
                .andExpect(status().isOk())
                .andExpect(content().string("Query Params: {key=value, otherKey=otherValue}"));

        // Verify request event
        assertThat(eventCaptureListener.getRequestEvents()).hasSize(1);
        HttpRequestEvent requestEvent = eventCaptureListener.getRequestEvents().getFirst();
        assertThat(requestEvent.getPath()).isEqualTo("/test/get-with-params");
        assertThat(requestEvent.getQueryParams()).containsEntry("key", List.of("value"));
        assertThat(requestEvent.getQueryParams()).containsEntry("otherKey", List.of("otherValue"));
        assertThat(requestEvent.getHeaders()).containsEntry("Custom-Header", List.of("HeaderValue"));

        // Verify response event
        assertThat(eventCaptureListener.getResponseEvents()).hasSize(1);
        HttpResponseEvent responseEvent = eventCaptureListener.getResponseEvents().getFirst();
        assertThat(responseEvent.getResponseBody()).isEqualTo("Query Params: {key=value, otherKey=otherValue}");
    }

    @Test
    void testGetWithPathVariablePublishesEvents() throws Exception {
        mockMvc.perform(get("/test/get-with-path/123")
                        .header("Another-Header", "AnotherValue"))
                .andExpect(status().isOk())
                .andExpect(content().string("Path Variable: 123"));

        // Verify request event
        assertThat(eventCaptureListener.getRequestEvents()).hasSize(1);
        HttpRequestEvent requestEvent = eventCaptureListener.getRequestEvents().getFirst();
        assertThat(requestEvent.getPath()).isEqualTo("/test/get-with-path/123");
        assertThat(requestEvent.getPathParams()).containsEntry("id", "123");
        assertThat(requestEvent.getHeaders()).containsEntry("Another-Header", List.of("AnotherValue"));

        // Verify response event
        assertThat(eventCaptureListener.getResponseEvents()).hasSize(1);
        HttpResponseEvent responseEvent = eventCaptureListener.getResponseEvents().getFirst();
        assertThat(responseEvent.getResponseBody()).isEqualTo("Path Variable: 123");
    }

    @Test
    void testPostWithQueryParamsAndHeadersPublishesEvents() throws Exception {
        String requestBody = "{\"name\": \"Test\"}";

        mockMvc.perform(post("/test/post-with-params/456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .param("key", "value")
                        .header("Post-Header", "HeaderValue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pathId").value("456"))
                .andExpect(jsonPath("$.queryParams.key").value("value"))
                .andExpect(jsonPath("$.body.name").value("Test"));

        // Verify request event
        assertThat(eventCaptureListener.getRequestEvents()).hasSize(1);
        HttpRequestEvent requestEvent = eventCaptureListener.getRequestEvents().getFirst();
        assertThat(requestEvent.getPath()).isEqualTo("/test/post-with-params/456");
        assertThat(requestEvent.getPathParams()).containsEntry("id", "456");
        assertThat(requestEvent.getQueryParams()).containsEntry("key", List.of("value"));
        assertThat(requestEvent.getHeaders()).containsEntry("Post-Header", List.of("HeaderValue"));

        var bodyJson = objectMapper.readTree(requestEvent.getRequestBody());
        assertThat(bodyJson.get("name").asText()).isEqualTo("Test");

        // Verify response event
        assertThat(eventCaptureListener.getResponseEvents()).hasSize(1);
        HttpResponseEvent responseEvent = eventCaptureListener.getResponseEvents().getFirst();
        var responseBodyJson = objectMapper.readTree(responseEvent.getResponseBody());
        assertThat(responseBodyJson.get("pathId").asText()).isEqualTo("456");
        assertThat(responseBodyJson.get("queryParams").get("key").get(0).asText()).isEqualTo("value");
        assertThat(responseBodyJson.get("body").get("name").asText()).isEqualTo("Test");
    }


}