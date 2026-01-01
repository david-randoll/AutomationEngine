package com.davidrandoll.automation.engine.spring.notification.actions;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.spring.notification.jackson.EmailList;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.boot.autoconfigure.mail.MailProperties;

import java.util.List;

/**
 * Context class for the SendEmail action.
 * <p>
 * Contains all configuration needed to send an email including recipients,
 * subject, body, attachments, and optional SMTP configuration.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        SendEmailActionContext.Fields.alias,
        SendEmailActionContext.Fields.description,
        SendEmailActionContext.Fields.from,
        SendEmailActionContext.Fields.to,
        SendEmailActionContext.Fields.cc,
        SendEmailActionContext.Fields.bcc,
        SendEmailActionContext.Fields.replyTo,
        SendEmailActionContext.Fields.subject,
        SendEmailActionContext.Fields.body,
        SendEmailActionContext.Fields.html,
        SendEmailActionContext.Fields.attachments,
        SendEmailActionContext.Fields.mailProperties
})
public class SendEmailActionContext implements IActionContext {

    /**
     * Unique identifier for this action
     */
    private String alias;

    /**
     * Human-readable description of what this action does
     */
    private String description;

    /**
     * Sender email address. If not specified, uses the default from configuration.
     */
    @ContextField(
            placeholder = "sender@example.com",
            helpText = "Sender email address. Falls back to automation-engine.notification.default-from if not specified"
    )
    private String from;

    /**
     * List of primary recipient email addresses (required)
     */
    @ContextField(
            helpText = "List of primary recipient email addresses"
    )
    @EmailList
    private List<String> to;

    /**
     * List of CC (carbon copy) recipient email addresses
     */
    @ContextField(
            helpText = "List of CC (carbon copy) recipient email addresses"
    )
    @EmailList
    private List<String> cc;

    /**
     * List of BCC (blind carbon copy) recipient email addresses
     */
    @ContextField(
            helpText = "List of BCC (blind carbon copy) recipient email addresses"
    )
    @EmailList
    private List<String> bcc;

    /**
     * Reply-to email address
     */
    @ContextField(
            placeholder = "reply@example.com",
            helpText = "Email address for replies"
    )
    private String replyTo;

    /**
     * Email subject line (required)
     */
    @ContextField(
            placeholder = "Email Subject",
            helpText = "Subject line of the email. Supports {{ }} templates"
    )
    private String subject;

    /**
     * Email body content (required)
     */
    @ContextField(
            widget = ContextField.Widget.TEXTAREA,
            helpText = "Body content of the email. Supports {{ }} templates. Can be HTML if 'html' is true"
    )
    private String body;

    /**
     * Whether the body content is HTML. Defaults to false (plain text)
     */
    @ContextField(
            widget = ContextField.Widget.SWITCH,
            helpText = "Set to true if the body contains HTML content"
    )
    private boolean html = false;

    /**
     * List of file attachments
     */
    @ContextField(
            helpText = "List of file attachments with base64 encoded content"
    )
    private List<Attachment> attachments;

    /**
     * Optional custom SMTP configuration. If not provided, uses Spring Mail default configuration.
     */
    @ContextField(
            helpText = "Custom SMTP server configuration. Uses Spring Mail defaults if not specified"
    )
    private MailProperties mailProperties;

    /**
     * Email attachment configuration
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    @JsonPropertyOrder({
            Attachment.Fields.filename,
            Attachment.Fields.contentType,
            Attachment.Fields.base64Content
    })
    public static class Attachment {

        /**
         * Filename for the attachment (required)
         */
        @ContextField(
                placeholder = "document.pdf",
                helpText = "Filename for the attachment as it will appear in the email"
        )
        private String filename;

        /**
         * MIME content type of the attachment
         */
        @ContextField(
                placeholder = "application/pdf",
                helpText = "MIME type of the attachment (e.g., application/pdf, image/png)"
        )
        private String contentType;

        /**
         * Base64 encoded content of the attachment
         */
        @ContextField(
                widget = ContextField.Widget.TEXTAREA,
                helpText = "Base64 encoded content of the file"
        )
        private String base64Content;
    }

}
