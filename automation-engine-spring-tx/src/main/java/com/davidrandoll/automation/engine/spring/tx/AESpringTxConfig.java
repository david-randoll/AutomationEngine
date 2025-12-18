package com.davidrandoll.automation.engine.spring.tx;

import com.davidrandoll.automation.engine.spring.tx.interceptors.TransactionalExecutionInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@ConditionalOnClass(PlatformTransactionManager.class)
public class AESpringTxConfig {
    
    @Bean
    @ConditionalOnMissingBean
    public TransactionalRunner transactionalRunner() {
        return new TransactionalRunner();
    }

    @Bean("transactionalExecutionInterceptor")
    @ConditionalOnMissingBean(name = "transactionalExecutionInterceptor", ignored = TransactionalExecutionInterceptor.class)
    public TransactionalExecutionInterceptor transactionalExecutionInterceptor(TransactionalRunner transactionalRunner) {
        return new TransactionalExecutionInterceptor(transactionalRunner);
    }
}
