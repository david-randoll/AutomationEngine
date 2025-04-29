package com.automation.engine.suppliers;

import com.automation.engine.conditional.AEConditionalOnMissingBeanType;
import com.automation.engine.core.conditions.ICondition;
import com.automation.engine.creator.conditions.ConditionNotFoundException;
import com.automation.engine.creator.conditions.IConditionSupplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@AEConditionalOnMissingBeanType(IConditionSupplier.class)
public class SpringConditionSupplier implements IConditionSupplier {
    private final ApplicationContext applicationContext;

    @Override
    public ICondition getCondition(String name) {
        try {
            var conditionName = "%sCondition".formatted(name);
            return applicationContext.getBean(conditionName, ICondition.class);
        } catch (NoSuchBeanDefinitionException e) {
            log.error("Bean {} not found", name, e);
            throw new ConditionNotFoundException(name);
        }
    }
}