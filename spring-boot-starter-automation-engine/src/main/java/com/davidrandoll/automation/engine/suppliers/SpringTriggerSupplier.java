package com.davidrandoll.automation.engine.suppliers;

import com.davidrandoll.automation.engine.core.triggers.ITrigger;
import com.davidrandoll.automation.engine.creator.triggers.ITriggerSupplier;
import com.davidrandoll.automation.engine.creator.triggers.TriggerNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(value = ITriggerSupplier.class, ignored = SpringTriggerSupplier.class)
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