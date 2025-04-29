package com.automation.engine.suppliers;

import com.automation.engine.conditional.AEConditionalOnMissingBean;
import com.automation.engine.core.variables.IVariable;
import com.automation.engine.creator.variables.IVariableSupplier;
import com.automation.engine.creator.variables.VariableNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@AEConditionalOnMissingBean(IVariableSupplier.class)
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