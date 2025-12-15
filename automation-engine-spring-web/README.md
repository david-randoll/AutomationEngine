# automation-engine-spring-web

HTTP/REST integration module providing web-based triggers and actions for AutomationEngine.

## Overview

This module enables automations to:

- **Trigger on HTTP requests** - React to incoming HTTP requests
- **Send HTTP requests** - Make outbound HTTP calls as actions
- **Respond to HTTP responses** - Trigger on HTTP response events
- **Match requests** - Filter by method, path, headers, query params, body

## Features

- ✅ **HTTP Request Trigger** - `onHttpRequest` - Match incoming requests
- ✅ **HTTP Response Trigger** - `onHttpResponse` - Match outgoing responses
- ✅ **HTTP Request Action** - `sendHttpRequest` - Send HTTP requests
- ✅ **Flexible Matching** - Methods, paths, headers, query params, request body
- ✅ **Template Support** - Use `{{ }}` expressions in all configurations
- ✅ **WebClient Integration** - Reactive HTTP client support

## Components

### 1. onHttpRequest Trigger

Triggers when an incoming HTTP request matches specified criteria.

**Parameters:**

- `method` / `methods` (string or array) - HTTP method(s) to match
- `path` / `paths` (string or array) - URL path pattern(s)
- `fullPath` / `fullPaths` (string or array) - Full URL pattern(s)
- `headers` / `header` (map) - Header key-value pairs
- `queryParams` / `query` (map) - Query parameter key-value pairs
- `pathParams` (map) - Path parameter key-value pairs
- `body` / `requestBody` (JsonNode) - Request body matcher

**Example - Match HTTP Method:**

```yaml
triggers:
  - trigger: onHttpRequest
    method: POST
actions:
  - action: logger
    message: "POST request received"
```

**Example - Match Multiple Methods:**

```yaml
triggers:
  - trigger: onHttpRequest
    methods: [GET, POST, PUT]
```

**Example - Match URL Path:**

```yaml
triggers:
  - trigger: onHttpRequest
    path: "/api/orders/.*"
actions:
  - action: logger
    message: "Order endpoint hit"
```

**Example - Match Headers:**

```yaml
triggers:
  - trigger: onHttpRequest
    headers:
      X-Auth-Type: Bearer
      Content-Type: application/json
actions:
  - action: logger
    message: "Authenticated request"
```

**Example - Match Query Parameters:**

```yaml
triggers:
  - trigger: onHttpRequest
    query:
      status: active
      category: premium
```

**Example - Match Request Body:**

```yaml
triggers:
  - trigger: onHttpRequest
    method: POST
    body:
      type: "user.created"
      priority: HIGH
actions:
  - action: sendEmail
    to: "admin@example.com"
    subject: "High priority user created"
```

**Example - Complex Matching:**

```yaml
triggers:
  - trigger: onHttpRequest
    method: POST
    path: "/api/webhooks/.*"
    headers:
      X-Webhook-Secret: "{{ secrets.webhookSecret }}"
    body:
      event: order.completed
actions:
  - action: processOrder
    orderId: "{{ event.requestBody.order.id }}"
```

### 2. onHttpResponse Trigger

Triggers when an HTTP response matches specified criteria.

**Parameters:**

- `statusCode` (int) - HTTP status code
- `headers` (map) - Response headers
- `body` (JsonNode) - Response body matcher

**Example:**

```yaml
triggers:
  - trigger: onHttpResponse
    statusCode: 200
    body:
      success: true
actions:
  - action: logger
    message: "Successful response received"
```

### 3. sendHttpRequest Action

Sends an HTTP request to a specified URL.

**Parameters:**

- `url` (string) - Target URL (supports templates)
- `method` (HttpMethod) - GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
- `headers` / `header` (map) - Request headers
- `contentType` (MediaType) - Content type (default: application/json)
- `body` (object) - Request body
- `storeToVariable` (string) - Variable name to store response
- `allowHttpEvent` (boolean) - Allow processing of HTTP events (default: false)

**Example - Simple GET:**

```yaml
actions:
  - action: sendHttpRequest
    url: "https://api.example.com/users"
    method: GET
    storeToVariable: users
```

**Example - POST with JSON Body:**

```yaml
actions:
  - action: sendHttpRequest
    url: "https://api.example.com/orders"
    method: POST
    contentType: application/json
    headers:
      Authorization: "Bearer {{ token }}"
    body:
      productId: "{{ event.productId }}"
      quantity: "{{ event.quantity }}"
      customer: "{{ event.customerId }}"
    storeToVariable: orderResponse
```

**Example - Using Response Data:**

```yaml
actions:
  - action: sendHttpRequest
    url: "https://api.example.com/user/{{ event.userId }}"
    method: GET
    storeToVariable: userData

  - action: logger
    message: "User: {{ userData.name }} - Email: {{ userData.email }}"
```

**Example - POST with Form Data:**

```yaml
actions:
  - action: sendHttpRequest
    url: "https://api.example.com/submit"
    method: POST
    contentType: application/x-www-form-urlencoded
    body:
      name: "{{ event.name }}"
      email: "{{ event.email }}"
```

**Example - Custom Headers:**

```yaml
actions:
  - action: sendHttpRequest
    url: "{{ apiUrl }}/webhook"
    method: POST
    headers:
      X-API-Key: "{{ secrets.apiKey }}"
      X-Request-ID: "{{ requestId }}"
      User-Agent: "AutomationEngine/1.0"
    body:
      event: "{{ event.type }}"
      data: "{{ event.payload }}"
```

## Events

### AEHttpRequestEvent

Event published for incoming HTTP requests.

**Properties:**

```java
public class AEHttpRequestEvent implements IEvent {
    private HttpMethodEnum method;
    private String fullUrl;
    private String path;
    private HttpHeaders headers;
    private MultiValueMap<String, String> queryParams;
    private Map<String, String> pathParams;
    private JsonNode requestBody;
}
```

**Accessing in Templates:**

```yaml
actions:
  - action: logger
    message: "Method: {{ event.method }}"

  - action: logger
    message: "Path: {{ event.path }}"

  - action: logger
    message: "Header: {{ event.headers['X-Custom-Header'] }}"

  - action: logger
    message: "Query: {{ event.queryParams.status }}"

  - action: logger
    message: "Body: {{ event.requestBody.userId }}"
```

### AEHttpResponseEvent

Event published for HTTP responses.

**Properties:**

```java
public class AEHttpResponseEvent implements IEvent {
    private int statusCode;
    private HttpHeaders headers;
    private JsonNode responseBody;
}
```

## Integration with Spring Web

### Automatic Request Capture

To enable automatic HTTP request event publishing, use the `spring-web-captor` integration:

```java
@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<HttpEventCaptorFilter> httpEventFilter(
            AutomationEngine engine) {

        FilterRegistrationBean<HttpEventCaptorFilter> registration =
            new FilterRegistrationBean<>();

        registration.setFilter(new HttpEventCaptorFilter(engine));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return registration;
    }
}
```

### Manual Event Publishing

Publish events manually in controllers:

```java
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final AutomationEngine engine;

    @PostMapping("/api/orders")
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request,
                                            HttpServletRequest httpRequest) {
        // Create event
        AEHttpRequestEvent event = AEHttpRequestEvent.builder()
            .method(HttpMethodEnum.POST)
            .path(httpRequest.getRequestURI())
            .requestBody(objectMapper.valueToTree(request))
            .build();

        // Trigger automations
        engine.publishEvent(event);

        // Process order...
        Order order = orderService.create(request);
        return ResponseEntity.ok(order);
    }
}
```

## Real-World Examples

### Webhook Handler

```yaml
alias: github-webhook-handler
description: Handles GitHub webhook events
triggers:
  - trigger: onHttpRequest
    method: POST
    path: "/webhooks/github"
    headers:
      X-GitHub-Event: push
conditions:
  - condition: template
    expression: "{{ event.requestBody.ref == 'refs/heads/main' }}"
actions:
  - action: logger
    message: "Push to main branch by {{ event.requestBody.pusher.name }}"

  - action: sendHttpRequest
    url: "{{ ciUrl }}/trigger"
    method: POST
    body:
      branch: main
      commit: "{{ event.requestBody.head_commit.id }}"
```

### API Request Logging

```yaml
alias: log-high-value-transactions
triggers:
  - trigger: onHttpRequest
    method: POST
    path: "/api/transactions"
    body:
      amount: "{{ amount > 10000 }}"
actions:
  - action: logger
    message: "High-value transaction: ${{ event.requestBody.amount }}"

  - action: sendEmail
    to: "finance@example.com"
    subject: "High-Value Transaction Alert"
    body: |
      Amount: ${{ event.requestBody.amount }}
      User: {{ event.requestBody.userId }}
      Time: {{ now }}
```

### Rate Limiting Check

```yaml
alias: check-rate-limit
triggers:
  - trigger: onHttpRequest
    path: "/api/.*"
actions:
  - action: sendHttpRequest
    url: "{{ rateLimitService }}/check"
    method: POST
    body:
      ip: "{{ event.headers['X-Forwarded-For'] }}"
      path: "{{ event.path }}"
    storeToVariable: rateLimitCheck

  - action: logger
    message: "Rate limit remaining: {{ rateLimitCheck.remaining }}"
```

### Service Health Check

```yaml
alias: health-check-monitor
triggers:
  - trigger: time
    cron: "*/5 * * * *" # Every 5 minutes
actions:
  - action: sendHttpRequest
    url: "{{ serviceUrl }}/health"
    method: GET
    storeToVariable: healthStatus

  - action: template
    expression: "{{ healthStatus.status != 'UP' }}"
    storeToVariable: isDown

  - action: sendEmail
    condition: "{{ isDown }}"
    to: "ops@example.com"
    subject: "Service Down Alert"
    body: "Service health check failed: {{ healthStatus }}"
```

## Configuration

### WebClient Customization

Customize the WebClient used by `sendHttpRequest`:

```java
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
            .defaultHeader("User-Agent", "AutomationEngine")
            .clientConnector(new ReactorClientHttpConnector(
                HttpClient.create()
                    .responseTimeout(Duration.ofSeconds(30))
            ));
    }
}
```

## Testing

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class HttpTriggerTest extends AutomationEngineTest {

    @Test
    void testHttpRequestTrigger() {
        var yaml = """
            alias: test-http
            triggers:
              - trigger: onHttpRequest
                method: POST
                path: "/api/test"
            actions:
              - action: logger
                message: "Request received"
            """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Simulate HTTP request
        AEHttpRequestEvent event = AEHttpRequestEvent.builder()
            .method(HttpMethodEnum.POST)
            .path("/api/test")
            .build();

        engine.publishEvent(event);

        // Verify
        assertThat(logAppender.getLoggedMessages())
            .anyMatch(msg -> msg.contains("Request received"));
    }
}
```

## Dependencies

- `automation-engine-spring` - Spring integration base
- `org.springframework.boot:spring-boot-starter-web` - Spring Web
- `org.springframework.boot:spring-boot-starter-webflux` - WebClient support
- `spring-web-captor` - HTTP request/response capture (optional)

## See Also

- **[automation-engine-spring](../automation-engine-spring/README.md)** - Spring base layer
- **[AI_PROMPT.md](../AI_PROMPT.md)** - HTTP trigger and action syntax reference
