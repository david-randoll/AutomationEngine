package com.davidrandoll.automation.engine.spring.notification.actions;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.notification.exceptions.SendEmailValidationException;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import jakarta.activation.DataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.ObjectUtils;

import java.util.Base64;
import java.util.Properties;

/**
 * Pluggable action for sending emails via SMTP.
 * <p>
 * This action supports:
 * <ul>
 *     <li>Multiple recipients (to, cc, bcc)</li>
 *     <li>HTML or plain text body</li>
 *     <li>File attachments (base64 encoded or from URL)</li>
 *     <li>Custom SMTP configuration per action or default from Spring Mail properties</li>
 * </ul>
 * </p>
 *
 * <p>Example YAML usage:</p>
 * <pre>
 * actions:
 *   - action: sendEmail
 *     to:
 *       - "user@example.com"
 *     subject: "Hello from Automation Engine"
 *     body: "<h1>Welcome!</h1><p>This is an automated email.</p>"
 *     html: true
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
public class SendEmailAction extends PluggableAction<SendEmailActionContext> {

    private final JavaMailSender defaultMailSender;
    private final MailProperties properties;

    @Override
    public void doExecute(EventContext ec, SendEmailActionContext ac) {
        log.debug("Executing SendEmailAction: {}", ac.getAlias());

        if (ObjectUtils.isEmpty(ac.getTo())) {
            throw new SendEmailValidationException("'to' email address is required");
        }
        if (ObjectUtils.isEmpty(ac.getSubject())) {
            throw new SendEmailValidationException("'subject' is required");
        }
        if (ObjectUtils.isEmpty(ac.getBody())) {
            throw new SendEmailValidationException("'body' is required");
        }

        try {
            JavaMailSender mailSender = createMailSender(ac.getMailProperties());
            MimeMessage message = mailSender.createMimeMessage();

            boolean hasAttachments = !ObjectUtils.isEmpty(ac.getAttachments());
            MimeMessageHelper helper = new MimeMessageHelper(message, hasAttachments, "UTF-8");

            // Set from address
            String fromAddress = !ObjectUtils.isEmpty(ac.getFrom()) ? ac.getFrom() : properties.getUsername();
            helper.setFrom(fromAddress);

            // Set recipients
            helper.setTo(ac.getTo().toArray(new String[0]));

            if (!ObjectUtils.isEmpty(ac.getCc())) {
                helper.setCc(ac.getCc().toArray(new String[0]));
            }

            if (!ObjectUtils.isEmpty(ac.getBcc())) {
                helper.setBcc(ac.getBcc().toArray(new String[0]));
            }

            // Set subject and body
            helper.setSubject(ac.getSubject());
            helper.setText(ac.getBody(), ac.isHtml());

            // Handle attachments
            if (hasAttachments) {
                addAttachments(helper, ac);
            }

            // Set reply-to if provided
            if (!ObjectUtils.isEmpty(ac.getReplyTo())) {
                helper.setReplyTo(ac.getReplyTo());
            }

            mailSender.send(message);
            log.info("Email sent successfully to: {}", ac.getTo());

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    /**
     * Resolves the JavaMailSender to use.
     * If custom SMTP configuration is provided in the action context, creates a new sender.
     * Otherwise, uses the default sender configured via Spring Mail properties.
     */
    private JavaMailSender createMailSender(@Nullable MailProperties ctxCred) {
        if (ctxCred == null || ObjectUtils.isEmpty(ctxCred.getHost())) return defaultMailSender;

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        if (ObjectUtils.isEmpty(ctxCred.getHost())) {
            throw new IllegalArgumentException("SMTP host is required (either in context.credentials or configuration properties)");
        }

        mailSender.setHost(ctxCred.getHost());
        mailSender.setPort(ctxCred.getPort());
        mailSender.setUsername(ctxCred.getUsername());
        mailSender.setPassword(ctxCred.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.putAll(ctxCred.getProperties());

        return mailSender;
    }

    /**
     * Adds attachments to the email message.
     */
    private void addAttachments(MimeMessageHelper helper, SendEmailActionContext ac) throws MessagingException {
        for (SendEmailActionContext.Attachment attachment : ac.getAttachments()) {
            if (ObjectUtils.isEmpty(attachment.getFilename())) {
                throw new IllegalArgumentException("Attachment filename is required");
            }

            if (!ObjectUtils.isEmpty(attachment.getBase64Content())) {
                // Decode base64 content
                byte[] content = Base64.getDecoder().decode(attachment.getBase64Content());
                String contentType = !ObjectUtils.isEmpty(attachment.getContentType())
                        ? attachment.getContentType()
                        : "application/octet-stream";

                DataSource dataSource = new ByteArrayDataSource(content, contentType);
                helper.addAttachment(attachment.getFilename(), dataSource);

            } else {
                throw new IllegalArgumentException("Attachment must have base64Content: " + attachment.getFilename());
            }
        }
    }
}