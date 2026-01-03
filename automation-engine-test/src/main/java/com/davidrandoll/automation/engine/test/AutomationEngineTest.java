package com.davidrandoll.automation.engine.test;

import ch.qos.logback.classic.Logger;
import com.davidrandoll.automation.engine.AutomationEngine;
import com.davidrandoll.automation.engine.creator.AutomationFactory;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = AutomationEngineApplication.class)
@AutoConfigureMockMvc
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AutomationEngineTest {
    @Autowired
    protected AutomationEngine engine;

    @Autowired
    protected AutomationFactory factory;

    protected TestLogAppender logAppender;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @LocalServerPort
    protected int port;

    @BeforeEach
    void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger("com.davidrandoll.automation.engine");
        
        // Remove any existing appenders to avoid duplicate logging
        logger.detachAndStopAllAppenders();
        
        // Create new appender for this test
        logAppender = new TestLogAppender();
        logAppender.setContext(logger.getLoggerContext());
        logAppender.start();  // Start before adding to logger
        logger.addAppender(logAppender);
        logger.setAdditive(false);  // Don't propagate to parent loggers
        logger.setLevel(ch.qos.logback.classic.Level.DEBUG);

        engine.removeAll();
    }
}