package com.davidrandoll.automation.engine.suppliers;

import com.davidrandoll.automation.engine.core.variables.IVariable;
import com.davidrandoll.automation.engine.creator.variables.IVariableSupplier;
import com.davidrandoll.automation.engine.creator.variables.VariableNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(value = IVariableSupplier.class, ignored = SpringVariableSupplier.class)
public class SpringVariableSupplier implements IVariableSupplier {
    private final ApplicationContext applicationContext;

    @Override
    public IVariable getVariable(String name) {
        try {
            var variableName = "%sVariable".formatted(name);
            return applicationContext.getBean(variableName, IVariable.class);
        } catch (NoSuchBeanDefinitionException e) {
            log.error("Bean {} not found", name, e);
            throw new VariableNotFoundException(name);
        }
    }
}