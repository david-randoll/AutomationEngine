package com.automation.engine.suppliers;

import com.automation.engine.conditional.AEConditionalOnMissingBeanType;
import com.automation.engine.core.actions.IAction;
import com.automation.engine.creator.actions.ActionNotFoundException;
import com.automation.engine.creator.actions.IActionSupplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@AEConditionalOnMissingBeanType(IActionSupplier.class)
public class SpringActionSupplier implements IActionSupplier {
    private final ApplicationContext applicationContext;

    @Override
    public IAction getAction(String name) throws ActionNotFoundException {
        try {
            var actionName = "%sAction".formatted(name);
            return applicationContext.getBean(actionName, IAction.class);
        } catch (NoSuchBeanDefinitionException e) {
            log.error("Bean {} not found", name, e);
            throw new ActionNotFoundException(name);
        }
    }
}