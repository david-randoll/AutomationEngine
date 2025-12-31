package com.davidrandoll.automation.engine.spring.notification.actions;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.modules.events.time_based.TimeBasedEvent;
import com.davidrandoll.automation.engine.spring.notification.AENotificationProperties;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalTime;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SendEmailActionTest extends AutomationEngineTest {

    @MockBean
    private JavaMailSender javaMailSender;

    @MockBean
    private AENotificationProperties notificationProperties;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUpMocks() {
        reset(javaMailSender);
        mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(notificationProperties.getDefaultFrom()).thenReturn("default@test.com");
    }

    @Test
    void testSendSimpleEmail() {
        var yaml = """
                alias: Send simple email
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendEmail
                    from: sender@test.com
                    to:
                      - recipient@test.com
                    subject: Test Subject
                    body: Test Body Content
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmailWithDefaultFrom() {
        var yaml = """
                alias: Send email with default from
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendEmail
                    to:
                      - recipient@test.com
                    subject: Test Subject
                    body: Test Body Content
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        // Should use default-from from properties
        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendHtmlEmail() {
        var yaml = """
                alias: Send HTML email
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendEmail
                    from: sender@test.com
                    to:
                      - recipient@test.com
                    subject: HTML Test
                    body: <h1>Hello</h1><p>This is HTML content</p>
                    html: true
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmailWithCcAndBcc() {
        var yaml = """
                alias: Send email with CC and BCC
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendEmail
                    from: sender@test.com
                    to:
                      - primary@test.com
                    cc:
                      - cc1@test.com
                      - cc2@test.com
                    bcc:
                      - bcc@test.com
                    subject: Multi-recipient Test
                    body: Test with CC and BCC
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmailWithReplyTo() {
        var yaml = """
                alias: Send email with reply-to
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendEmail
                    from: sender@test.com
                    to:
                      - recipient@test.com
                    replyTo: reply@test.com
                    subject: Reply-to Test
                    body: Test with reply-to
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmailWithAttachment() {
        String base64Content = Base64.getEncoder().encodeToString("Hello, World!".getBytes());

        var yaml = """
                alias: Send email with attachment
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendEmail
                    from: sender@test.com
                    to:
                      - recipient@test.com
                    subject: Attachment Test
                    body: See attached file
                    attachments:
                      - filename: test.txt
                        contentType: text/plain
                        base64Content: %s
                """.formatted(base64Content);

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmailWithAttachmentDefaultContentType() {
        String base64Content = Base64.getEncoder().encodeToString("Binary content".getBytes());

        var yaml = """
                alias: Send email with attachment default content type
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendEmail
                    from: sender@test.com
                    to:
                      - recipient@test.com
                    subject: Attachment Test
                    body: See attached file
                    attachments:
                      - filename: data.bin
                        base64Content: %s
                """.formatted(base64Content);

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void testMissingToRecipientsSkipsExecution() {
        var yaml = """
                alias: Missing to recipients
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendEmail
                    from: sender@test.com
                    subject: Test Subject
                    body: Test Body
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        // Should not send because 'to' is missing
        verify(javaMailSender, never()).createMimeMessage();
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testMissingSubjectSkipsExecution() {
        var yaml = """
                alias: Missing subject
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendEmail
                    from: sender@test.com
                    to:
                      - recipient@test.com
                    body: Test Body
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        // Should not send because 'subject' is missing
        verify(javaMailSender, never()).createMimeMessage();
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testMissingBodySkipsExecution() {
        var yaml = """
                alias: Missing body
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendEmail
                    from: sender@test.com
                    to:
                      - recipient@test.com
                    subject: Test Subject
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        // Should not send because 'body' is missing
        verify(javaMailSender, never()).createMimeMessage();
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmailWithCustomSmtpConfig() {
        var yaml = """
                alias: Send email with custom SMTP
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendEmail
                    from: custom@test.com
                    to:
                      - recipient@test.com
                    subject: Custom SMTP Test
                    body: Test with custom SMTP
                    smtpConfig:
                      host: custom.smtp.com
                      port: 587
                      username: customuser
                      password: custompass
                      properties:
                        mail.smtp.auth: "true"
                        mail.smtp.starttls.enable: "true"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));

        // The action creates its own JavaMailSenderImpl when custom config is provided
        // Since the custom SMTP server doesn't exist, it should throw a MailSendException
        assertThatThrownBy(() -> engine.publishEvent(context))
                .isInstanceOf(org.springframework.mail.MailSendException.class)
                .hasMessageContaining("custom.smtp.com");
    }

    @Test
    void testAttachmentWithoutFilenameThrowsException() {
        String base64Content = Base64.getEncoder().encodeToString("content".getBytes());

        var yaml = """
                alias: Attachment without filename
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendEmail
                    from: sender@test.com
                    to:
                      - recipient@test.com
                    subject: Test
                    body: Test
                    attachments:
                      - base64Content: %s
                """.formatted(base64Content);

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));

        assertThatThrownBy(() -> engine.publishEvent(context))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Attachment filename is required");
    }

    @Test
    void testAttachmentWithoutBase64ContentThrowsException() {
        var yaml = """
                alias: Attachment without content
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendEmail
                    from: sender@test.com
                    to:
                      - recipient@test.com
                    subject: Test
                    body: Test
                    attachments:
                      - filename: test.txt
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));

        assertThatThrownBy(() -> engine.publishEvent(context))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Attachment must have base64Content");
    }

    @Test
    void testSendEmailWithMultipleRecipients() {
        var yaml = """
                alias: Send to multiple recipients
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendEmail
                    from: sender@test.com
                    to:
                      - recipient1@test.com
                      - recipient2@test.com
                      - recipient3@test.com
                    subject: Multiple Recipients Test
                    body: Test with multiple recipients
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmailWithTemplateVariables() {
        var yaml = """
                alias: Send email with template
                variables:
                  - name: userName
                    value: John Doe
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendEmail
                    from: sender@test.com
                    to:
                      - recipient@test.com
                    subject: "Hello {{ variables.userName }}"
                    body: "Welcome {{ variables.userName }} to our service!"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(any(MimeMessage.class));
    }
}
