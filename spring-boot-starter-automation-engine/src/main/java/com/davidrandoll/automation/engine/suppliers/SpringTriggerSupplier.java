package com.davidrandoll.automation.engine.suppliers;

import com.davidrandoll.automation.engine.core.triggers.ITrigger;
import com.davidrandoll.automation.engine.creator.triggers.ITriggerSupplier;
import com.davidrandoll.automation.engine.creator.triggers.TriggerNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

@Slf4j
@RequiredArgsConstructor
public class SpringTriggerSupplier implements ITriggerSupplier {
    private final ApplicationContext applicationContext;

    @Override
    public ITrigger getTrigger(String name) {
        try {
            if (name.endsWith("Trigger")) {
                return applicationContext.getBean(name, ITrigger.class);
            }
            var triggerName = "%sTrigger".formatted(name);
            return applicationContext.getBean(triggerName, ITrigger.class);
        } catch (NoSuchBeanDefinitionException e) {
            log.error("Bean {} not found", name, e);
            throw new TriggerNotFoundException(name);
        }
    }
}