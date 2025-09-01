package com.davidrandoll.automation.engine.suppliers;

import com.davidrandoll.automation.engine.core.conditions.ICondition;
import com.davidrandoll.automation.engine.creator.conditions.ConditionNotFoundException;
import com.davidrandoll.automation.engine.creator.conditions.IConditionSupplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

@Slf4j
@RequiredArgsConstructor
public class SpringConditionSupplier implements IConditionSupplier {
    private final ApplicationContext applicationContext;

    @Override
    public ICondition getCondition(String name) {
        try {
            if (name.endsWith("Condition")) {
                return applicationContext.getBean(name, ICondition.class);
            }
            var conditionName = "%sCondition".formatted(name);
            return applicationContext.getBean(conditionName, ICondition.class);
        } catch (NoSuchBeanDefinitionException e) {
            log.error("Bean {} not found", name, e);
            throw new ConditionNotFoundException(name);
        }
    }
}