package com.davidrandoll.automation.engine.spring.notification.actions;

import com.davidrandoll.automation.engine.AutomationEngine;
import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.creator.AutomationFactory;
import com.davidrandoll.automation.engine.spring.modules.events.time_based.TimeBasedEvent;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalTime;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = com.davidrandoll.automation.engine.test.AutomationEngineApplication.class)
@Import(SendEmailActionIntegrationTest.GreenMailTestConfig.class)
class SendEmailActionIntegrationTest {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication());

    @Autowired
    private AutomationEngine engine;

    @Autowired
    private AutomationFactory factory;

    @DynamicPropertySource
    static void mailProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> ServerSetupTest.SMTP.getPort());
        registry.add("spring.mail.username", () -> "default@test.com");
    }

    @BeforeEach
    void setUp() {
        engine.removeAll();
        greenMail.reset();
    }

    @Test
    void testSendSimpleEmailIntegration() throws Exception {
        var yaml = """
                alias: Integration test - simple email
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendEmail
                    from: sender@test.com
                    to:
                      - recipient@test.com
                    subject: Integration Test Subject
                    body: This is the email body from integration test
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        // Wait for email to be received
        assertThat(greenMail.waitForIncomingEmail(5000, 1)).isTrue();

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);

        MimeMessage message = messages[0];
        assertThat(message.getSubject()).isEqualTo("Integration Test Subject");
        assertThat(message.getFrom()[0].toString()).isEqualTo("sender@test.com");
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo("recipient@test.com");
        assertThat(GreenMailUtil.getBody(message)).contains("This is the email body from integration test");
    }

    @Test
    void testSendHtmlEmailIntegration() throws Exception {
        var yaml = """
                alias: Integration test - HTML email
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendEmail
                    from: sender@test.com
                    to:
                      - recipient@test.com
                    subject: HTML Email Test
                    body: <h1>Hello World</h1><p>This is <strong>HTML</strong> content</p>
                    html: true
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        assertThat(greenMail.waitForIncomingEmail(5000, 1)).isTrue();

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);

        MimeMessage message = messages[0];
        assertThat(message.getSubject()).isEqualTo("HTML Email Test");
        String body = GreenMailUtil.getBody(message);
        assertThat(body).contains("<h1>Hello World</h1>");
    }

    @Test
    void testSendEmailWithMultipleRecipientsIntegration() throws Exception {
        var yaml = """
                alias: Integration test - multiple recipients
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendEmail
                    from: sender@test.com
                    to:
                      - recipient1@test.com
                      - recipient2@test.com
                    cc:
                      - cc@test.com
                    bcc:
                      - bcc@test.com
                    subject: Multiple Recipients
                    body: Email to multiple recipients
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        // Expect 4 messages (2 to, 1 cc, 1 bcc)
        assertThat(greenMail.waitForIncomingEmail(5000, 4)).isTrue();

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(4);

        // Verify all messages have same subject
        for (MimeMessage msg : messages) {
            assertThat(msg.getSubject()).isEqualTo("Multiple Recipients");
        }
    }

    @Test
    void testSendEmailWithAttachmentIntegration() throws Exception {
        String testContent = "This is attachment content";
        String base64Content = Base64.getEncoder().encodeToString(testContent.getBytes());

        var yaml = """
                alias: Integration test - attachment
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendEmail
                    from: sender@test.com
                    to:
                      - recipient@test.com
                    subject: Email with Attachment
                    body: Please see attached file
                    attachments:
                      - filename: test-file.txt
                        contentType: text/plain
                        base64Content: %s
                """.formatted(base64Content);

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        assertThat(greenMail.waitForIncomingEmail(5000, 1)).isTrue();

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);

        MimeMessage message = messages[0];
        assertThat(message.getSubject()).isEqualTo("Email with Attachment");
        
        // Verify attachment is present by checking content type
        assertThat(message.getContentType()).contains("multipart");
    }

    @Test
    void testSendEmailWithDefaultFromIntegration() throws Exception {
        var yaml = """
                alias: Integration test - default from
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendEmail
                    to:
                      - recipient@test.com
                    subject: Default From Test
                    body: This email uses default from address
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        assertThat(greenMail.waitForIncomingEmail(5000, 1)).isTrue();

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);

        MimeMessage message = messages[0];
        assertThat(message.getFrom()[0].toString()).isEqualTo("default@test.com");
    }

    @Test
    void testSendEmailWithReplyToIntegration() throws Exception {
        var yaml = """
                alias: Integration test - reply-to
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendEmail
                    from: sender@test.com
                    to:
                      - recipient@test.com
                    replyTo: support@test.com
                    subject: Reply-To Test
                    body: Reply to support
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        assertThat(greenMail.waitForIncomingEmail(5000, 1)).isTrue();

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);

        MimeMessage message = messages[0];
        assertThat(message.getReplyTo()[0].toString()).isEqualTo("support@test.com");
    }

    @Configuration
    static class GreenMailTestConfig {
        @Bean
        @Primary
        public JavaMailSender greenMailJavaMailSender() {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost("localhost");
            mailSender.setPort(ServerSetupTest.SMTP.getPort());
            return mailSender;
        }

        @Bean
        @Primary
        public MailProperties greenMailProperties() {
            MailProperties props = new MailProperties();
            props.setHost("localhost");
            props.setPort(ServerSetupTest.SMTP.getPort());
            props.setUsername("default@test.com");
            return props;
        }
    }
}
