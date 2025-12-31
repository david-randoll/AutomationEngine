package com.davidrandoll.automation.engine.spring.notification.actions;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public boolean canExecute(EventContext ec, SendEmailActionContext ac) {
        if (ObjectUtils.isEmpty(ac.getTo())) {
            log.warn("SendEmailAction requires at least one 'to' recipient: {}", ac.getAlias());
            return false;
        }

        if (ObjectUtils.isEmpty(ac.getSubject())) {
            log.warn("SendEmailAction requires a 'subject': {}", ac.getAlias());
            return false;
        }

        if (ObjectUtils.isEmpty(ac.getBody())) {
            log.warn("SendEmailAction requires a 'body': {}", ac.getAlias());
            return false;
        }

        // Check that 'from' is available either in context or properties
        if (ObjectUtils.isEmpty(ac.getFrom()) && ObjectUtils.isEmpty(properties.getUsername())) {
            log.warn("SendEmailAction requires a 'from' address either in action or spring.mail.username configuration: {}", ac.getAlias());
            return false;
        }

        return true;
    }

    @Override
    public void doExecute(EventContext ec, SendEmailActionContext ac) {
        log.debug("Executing SendEmailAction: {}", ac.getAlias());

        try {
            JavaMailSender mailSender = resolveMailSender(ac);
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
    private JavaMailSender resolveMailSender(SendEmailActionContext ac) {
        if (ac.getMailProperties() == null) {
            return defaultMailSender;
        }

        org.springframework.boot.autoconfigure.mail.MailProperties config = ac.getMailProperties();
        JavaMailSenderImpl customSender = new JavaMailSenderImpl();

        if (!ObjectUtils.isEmpty(config.getHost())) {
            customSender.setHost(config.getHost());
        }

        if (config.getPort() != null && config.getPort() > 0) {
            customSender.setPort(config.getPort());
        }

        if (!ObjectUtils.isEmpty(config.getUsername())) {
            customSender.setUsername(config.getUsername());
        }

        if (!ObjectUtils.isEmpty(config.getPassword())) {
            customSender.setPassword(config.getPassword());
        }

        // Set Java Mail properties if provided
        if (config.getProperties() != null && !config.getProperties().isEmpty()) {
            Properties props = new Properties();
            props.putAll(config.getProperties());
            customSender.setJavaMailProperties(props);
        }

        return customSender;
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
