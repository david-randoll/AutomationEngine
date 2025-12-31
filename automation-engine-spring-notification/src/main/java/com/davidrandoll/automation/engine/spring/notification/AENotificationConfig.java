package com.davidrandoll.automation.engine.spring.notification;

import com.davidrandoll.automation.engine.spring.notification.actions.SendEmailAction;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
@ConditionalOnClass(JavaMailSender.class)
@EnableConfigurationProperties(AENotificationProperties.class)
public class AENotificationConfig {

    @Bean("sendEmailAction")
    @ConditionalOnMissingBean(name = "sendEmailAction", ignored = SendEmailAction.class)
    public SendEmailAction sendEmailAction(JavaMailSender defaultMailSender,
                                            AENotificationProperties properties) {
        return new SendEmailAction(defaultMailSender, properties);
    }
}
