package com.davidrandoll.automation.engine.suppliers;

import com.davidrandoll.automation.engine.core.actions.IAction;
import com.davidrandoll.automation.engine.creator.actions.ActionNotFoundException;
import com.davidrandoll.automation.engine.creator.actions.IActionSupplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

@Slf4j
@RequiredArgsConstructor
public class SpringActionSupplier implements IActionSupplier {
    private final ApplicationContext applicationContext;

    @Override
    public IAction getAction(String name) throws ActionNotFoundException {
        try {
            if (name.endsWith("Action")) {
                return applicationContext.getBean(name, IAction.class);
            }
            var actionName = "%sAction".formatted(name);
            return applicationContext.getBean(actionName, IAction.class);
        } catch (NoSuchBeanDefinitionException e) {
            log.error("Bean {} not found", name, e);
            throw new ActionNotFoundException(name);
        }
    }
}