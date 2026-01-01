# Automation Engine Notification Module

This module provides email notification capabilities for the Automation Engine.

## Features

- **sendEmail Action**: Send emails with support for:
  - Multiple recipients (to, cc, bcc)
  - HTML or plain text body
  - File attachments (base64 encoded)
  - Custom SMTP configuration per action
  - Default SMTP configuration via Spring Mail properties

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.davidrandoll</groupId>
    <artifactId>automation-engine-spring-notification</artifactId>
    <version>${automation-engine.version}</version>
</dependency>
```

## Configuration

### Default Mail Server Configuration

Configure your default SMTP server using Spring Mail properties:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

automation-engine:
  notification:
    default-from: noreply@example.com
```

## Usage

### Basic Email

```yaml
alias: Send welcome email
triggers:
  - trigger: alwaysTrue
actions:
  - action: sendEmail
    to:
      - "user@example.com"
    subject: "Welcome!"
    body: "Hello, welcome to our service!"
```

### HTML Email with Template Variables

```yaml
alias: Send order confirmation
triggers:
  - trigger: alwaysTrue
actions:
  - action: sendEmail
    to:
      - "{{ event.customerEmail }}"
    subject: "Order Confirmation #{{ event.orderId }}"
    body: |
      <h1>Thank you for your order!</h1>
      <p>Order ID: {{ event.orderId }}</p>
      <p>Total: ${{ event.total }}</p>
    html: true
```

### Email with Attachments

```yaml
alias: Send report email
triggers:
  - trigger: alwaysTrue
actions:
  - action: sendEmail
    to:
      - "manager@example.com"
    subject: "Monthly Report"
    body: "Please find the attached report."
    attachments:
      - filename: report.pdf
        contentType: application/pdf
        base64Content: "{{ variables.reportBase64 }}"
```

### Email with Custom SMTP Configuration

```yaml
alias: Send via custom SMTP
triggers:
  - trigger: alwaysTrue
actions:
  - action: sendEmail
    from: "custom-sender@otherdomain.com"
    to:
      - "recipient@example.com"
    subject: "Custom SMTP Email"
    body: "This email is sent via a custom SMTP server."
    smtpConfig:
      host: smtp.otherdomain.com
      port: 587
      username: custom-user
      password: custom-password
      properties:
        mail.smtp.auth: "true"
        mail.smtp.starttls.enable: "true"
```

### Email with CC and BCC

```yaml
alias: Send to multiple recipients
triggers:
  - trigger: alwaysTrue
actions:
  - action: sendEmail
    from: "sender@example.com"
    to:
      - "primary@example.com"
    cc:
      - "manager@example.com"
      - "supervisor@example.com"
    bcc:
      - "archive@example.com"
    replyTo: "support@example.com"
    subject: "Team Update"
    body: "This is an update for the team."
```

## Action Context Properties

| Property      | Type             | Required | Description                                                                                |
| ------------- | ---------------- | -------- | ------------------------------------------------------------------------------------------ |
| `alias`       | String           | No       | Unique identifier for this action                                                          |
| `description` | String           | No       | Human-readable description                                                                 |
| `from`        | String           | No\*     | Sender email address (\*required if `automation-engine.notification.default-from` not set) |
| `to`          | List<String>     | Yes      | List of primary recipient email addresses                                                  |
| `cc`          | List<String>     | No       | List of CC recipient email addresses                                                       |
| `bcc`         | List<String>     | No       | List of BCC recipient email addresses                                                      |
| `replyTo`     | String           | No       | Reply-to email address                                                                     |
| `subject`     | String           | Yes      | Email subject line                                                                         |
| `body`        | String           | Yes      | Email body content                                                                         |
| `html`        | boolean          | No       | Whether body is HTML (default: false)                                                      |
| `attachments` | List<Attachment> | No       | List of file attachments                                                                   |
| `smtpConfig`  | SmtpConfig       | No       | Custom SMTP configuration                                                                  |

### Attachment Properties

| Property        | Type   | Required | Description                                   |
| --------------- | ------ | -------- | --------------------------------------------- |
| `filename`      | String | Yes      | Filename as it appears in email               |
| `contentType`   | String | No       | MIME type (default: application/octet-stream) |
| `base64Content` | String | Yes      | Base64 encoded file content                   |

### SmtpConfig Properties

| Property     | Type                | Required | Description                     |
| ------------ | ------------------- | -------- | ------------------------------- |
| `host`       | String              | No       | SMTP server hostname            |
| `port`       | Integer             | No       | SMTP server port                |
| `username`   | String              | No       | Authentication username         |
| `password`   | String              | No       | Authentication password         |
| `properties` | Map<String, String> | No       | Additional Java Mail properties |
