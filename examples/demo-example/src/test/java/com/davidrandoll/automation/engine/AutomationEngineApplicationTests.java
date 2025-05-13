package com.davidrandoll.automation.engine;

import com.davidrandoll.automation.engine.example.DemoApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = DemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class AutomationEngineApplicationTests {

    @Test
    void contextLoads() {
    }

}
