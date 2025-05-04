package com.davidrandoll.automation.engine.http;

import ch.qos.logback.classic.Logger;
import com.davidrandoll.automation.engine.core.AutomationEngine;
import com.davidrandoll.automation.engine.creator.AutomationCreator;
import com.davidrandoll.automation.engine.http.publisher.EventCaptureListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = AutomationEngineHttpApplication.class)
@AutoConfigureMockMvc
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AutomationEngineTest {
    @Autowired
    protected AutomationEngine engine;

    @Autowired
    protected AutomationCreator factory;

    protected TestLogAppender logAppender;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected EventCaptureListener eventCaptureListener; // Capture published events

    @Autowired
    protected ObjectMapper objectMapper;

    @LocalServerPort
    protected int port;

    @BeforeEach
    void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger("com.automation.engine");
        logAppender = new TestLogAppender();
        logger.addAppender(logAppender);
        logAppender.start();

        engine.removeAll();
        eventCaptureListener.clearEvents(); // Reset events before each test
    }
}