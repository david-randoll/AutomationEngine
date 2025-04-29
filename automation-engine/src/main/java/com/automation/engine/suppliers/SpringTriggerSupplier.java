package com.automation.engine.suppliers;

import com.automation.engine.conditional.AEConditionalOnMissingBean;
import com.automation.engine.core.triggers.ITrigger;
import com.automation.engine.creator.triggers.ITriggerSupplier;
import com.automation.engine.creator.triggers.TriggerNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@AEConditionalOnMissingBean(ITriggerSupplier.class)
public class SpringTriggerSupplier implements ITriggerSupplier {
    private final ApplicationContext applicationContext;

    @Override
    public ITrigger getTrigger(String name) {
        try {
            var triggerName = "%sTrigger".formatted(name);
            return applicationContext.getBean(triggerName, ITrigger.class);
        } catch (NoSuchBeanDefinitionException e) {
            log.error("Bean {} not found", name, e);
            throw new TriggerNotFoundException(name);
        }
    }
}