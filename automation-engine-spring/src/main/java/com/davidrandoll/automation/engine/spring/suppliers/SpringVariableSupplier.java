package com.davidrandoll.automation.engine.spring.suppliers;

import com.davidrandoll.automation.engine.core.variables.IVariable;
import com.davidrandoll.automation.engine.creator.variables.IVariableSupplier;
import com.davidrandoll.automation.engine.creator.variables.VariableNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

@Slf4j
@RequiredArgsConstructor
public class SpringVariableSupplier implements IVariableSupplier {
    private final ApplicationContext applicationContext;

    @Override
    public IVariable getVariable(String name) {
        try {
            if (name.endsWith("Variable")) {
                return applicationContext.getBean(name, IVariable.class);
            }
            var variableName = "%sVariable".formatted(name);
            return applicationContext.getBean(variableName, IVariable.class);
        } catch (NoSuchBeanDefinitionException e) {
            log.error("Bean {} not found", name, e);
            throw new VariableNotFoundException(name);
        }
    }
}