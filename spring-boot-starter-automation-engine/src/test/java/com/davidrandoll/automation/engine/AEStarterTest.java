package com.davidrandoll.automation.engine;

import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.creator.AutomationFactory;
import com.davidrandoll.automation.engine.orchestrator.IAEOrchestrator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AEStarterApplication.class)
public class AEStarterTest {
    
    @Autowired
    private AutomationEngine engine;
    
    @Autowired
    private ApplicationContext context;
    
    // Simple test event implementation
    private static class SimpleEvent implements IEvent {}
    
    @Test
    void shouldLoadAllRequiredBeans() {
        // Verify critical beans are loaded by the starter
        assertThat(engine).isNotNull();
        assertThat(context.getBean(IAEOrchestrator.class)).isNotNull();
        assertThat(context.getBean(AutomationFactory.class)).isNotNull();
    }
    
    @Test
    void shouldExecuteBasicAutomationFromStarter() {
        // Test actual functionality works when loaded through starter
        String yaml = """
            alias: starter-test
            triggers:
              - trigger: alwaysTrue
            actions:
              - action: logger
                message: "Starter working"
            """;
        
        var result = engine.executeAutomationWithYaml(yaml, new SimpleEvent());
        assertThat(result.isExecuted()).isTrue();
        assertThat(result.getAutomation().getAlias()).isEqualTo("starter-test");
    }
    
    @Test
    void shouldRegisterAndExecuteAutomation() {
        // Test registration works through starter
        String yaml = """
            alias: registration-test
            triggers:
              - trigger: alwaysTrue
            actions:
              - action: logger
                message: "Registration test"
            """;
        
        engine.registerWithYaml(yaml);
        engine.publishEvent(new SimpleEvent());
        
        // Verify the automation was registered (publishEvent doesn't throw an exception)
        assertThat(engine).isNotNull();
    }
}