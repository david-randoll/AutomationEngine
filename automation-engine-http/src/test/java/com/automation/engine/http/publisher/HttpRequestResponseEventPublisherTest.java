package com.automation.engine.http.publisher;

import com.automation.engine.http.AutomationEngineHttpApplication;
import com.automation.engine.http.event.HttpMethodEnum;
import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.http.event.HttpResponseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
        HttpRequestEvent requestEvent = eventCaptureListener.getRequestEvents().get(0);
        assertThat(requestEvent.getPath()).isEqualTo("/test/get");
        assertThat(requestEvent.getMethod()).isEqualTo(HttpMethodEnum.GET);

        // Verify response event
        assertThat(eventCaptureListener.getResponseEvents()).hasSize(1);
        HttpResponseEvent responseEvent = eventCaptureListener.getResponseEvents().get(0);
        assertThat(responseEvent.getResponseBody().asText()).isEqualTo("GET response");
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
        HttpRequestEvent requestEvent = eventCaptureListener.getRequestEvents().get(0);
        assertThat(requestEvent.getPath()).isEqualTo("/test/post");
        assertThat(requestEvent.getMethod()).isEqualTo(HttpMethodEnum.POST);

        // Verify response event
        assertThat(eventCaptureListener.getResponseEvents()).hasSize(1);
        HttpResponseEvent responseEvent = eventCaptureListener.getResponseEvents().get(0);
        assertThat(responseEvent.getResponseBody().get("key").asText()).isEqualTo("value");
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
        HttpRequestEvent requestEvent = eventCaptureListener.getRequestEvents().get(0);
        assertThat(requestEvent.getPath()).isEqualTo("/test/put");
        assertThat(requestEvent.getMethod()).isEqualTo(HttpMethodEnum.PUT);

        // Verify response event
        assertThat(eventCaptureListener.getResponseEvents()).hasSize(1);
        HttpResponseEvent responseEvent = eventCaptureListener.getResponseEvents().get(0);
        assertThat(responseEvent.getResponseBody().asText()).isEqualTo("Received: Updated content");
    }

    @Test
    void testDeleteRequestPublishesEvents() throws Exception {
        mockMvc.perform(delete("/test/delete"))
                .andExpect(status().isNoContent());

        // Verify request event
        assertThat(eventCaptureListener.getRequestEvents()).hasSize(1);
        HttpRequestEvent requestEvent = eventCaptureListener.getRequestEvents().get(0);
        assertThat(requestEvent.getPath()).isEqualTo("/test/delete");
        assertThat(requestEvent.getMethod()).isEqualTo(HttpMethodEnum.DELETE);

        // Verify response event
        assertThat(eventCaptureListener.getResponseEvents()).hasSize(1);
        HttpResponseEvent responseEvent = eventCaptureListener.getResponseEvents().get(0);
        assertThat(responseEvent.getResponseStatus()).isEqualTo(HttpStatus.NO_CONTENT);
    }

}