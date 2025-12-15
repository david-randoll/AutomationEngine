# automation-engine-spring-jdbc

Database/JDBC integration module providing database-based triggers and actions for AutomationEngine.

## Overview

This module enables automations to:

- **Trigger on query results** - Execute automations when database queries return specific data
- **Execute queries** - Run SELECT queries and store results
- **Execute updates** - Run INSERT, UPDATE, DELETE statements
- **Condition checking** - Verify conditions based on database state

## Features

- ✅ **JDBC Query Trigger** - `onJdbcQuery` - Trigger based on query results
- ✅ **JDBC Query Action** - `jdbcQuery` - Execute SELECT queries
- ✅ **JDBC Execute Action** - `jdbcExecute` - Execute INSERT/UPDATE/DELETE
- ✅ **JDBC Query Condition** - `onJdbcQuery` - Conditional logic based on queries
- ✅ **Named Parameters** - Use `:paramName` syntax in SQL
- ✅ **Template Support** - Use `{{ }}` expressions in SQL and parameters

## Components

### 1. onJdbcQuery Trigger

Triggers when a database query result matches a specified condition.

**Parameters:**

- `query` / `sql` / `statement` (string) - SQL query to execute
- `params` / `parameters` / `queryParams` (map) - Named parameters
- `expression` (string) - Pebble template expression to evaluate (must return boolean)

**Example - Check for Pending Tasks:**

```yaml
triggers:
  - trigger: onJdbcQuery
    query: "SELECT COUNT(*) as count FROM tasks WHERE completed = false"
    expression: "{{ result[0].count > 0 }}"
actions:
  - action: logger
    message: "Found {{ result[0].count }} pending tasks"
```

**Example - With Parameters:**

```yaml
triggers:
  - trigger: onJdbcQuery
    query: "SELECT * FROM orders WHERE user_id = :userId AND status = :status"
    params:
      userId: "{{ event.userId }}"
      status: "PENDING"
    expression: "{{ result.size > 0 }}"
```

**Example - Complex Expression:**

```yaml
triggers:
  - trigger: onJdbcQuery
    query: "SELECT SUM(amount) as total FROM transactions WHERE date = CURRENT_DATE"
    expression: "{{ result[0].total > 100000 }}"
actions:
  - action: sendEmail
    to: "finance@example.com"
    subject: "Daily Transactions Exceeded Threshold"
    body: "Total: ${{ result[0].total }}"
```

### 2. jdbcQuery Action

Executes a SELECT query and stores the result in a variable.

**Parameters:**

- `query` / `sql` (string) - SQL SELECT query
- `params` / `parameters` (map) - Named parameters
- `variable` / `storeToVariable` (string) - Variable name to store result
- `mode` (string) - `"list"` (default) or `"single"` for single row

**Example - Query List:**

```yaml
actions:
  - action: jdbcQuery
    query: "SELECT * FROM users WHERE active = true"
    variable: activeUsers
    mode: list

  - action: logger
    message: "Found {{ activeUsers.size }} active users"
```

**Example - Query Single Row:**

```yaml
actions:
  - action: jdbcQuery
    query: "SELECT * FROM users WHERE id = :userId"
    params:
      userId: "{{ event.userId }}"
    variable: user
    mode: single

  - action: logger
    message: "User: {{ user.name }} - {{ user.email }}"
```

**Example - Using Result in Subsequent Actions:**

```yaml
actions:
  - action: jdbcQuery
    query: "SELECT email FROM users WHERE role = 'ADMIN'"
    variable: admins

  - action: sendEmail
    to: "{{ admins[0].email }}"
    subject: "Alert"
    body: "System notification"
```

### 3. jdbcExecute Action

Executes an INSERT, UPDATE, or DELETE statement.

**Parameters:**

- `query` / `sql` / `statement` (string) - SQL statement
- `params` / `parameters` (map) - Named parameters

**Example - Insert:**

```yaml
actions:
  - action: jdbcExecute
    query: |
      INSERT INTO audit_log (user_id, action, timestamp)
      VALUES (:userId, :action, :timestamp)
    params:
      userId: "{{ event.userId }}"
      action: "{{ event.action }}"
      timestamp: "{{ now }}"
```

**Example - Update:**

```yaml
actions:
  - action: jdbcExecute
    query: "UPDATE users SET last_login = :now WHERE id = :userId"
    params:
      userId: "{{ event.userId }}"
      now: "{{ now }}"
```

**Example - Delete:**

```yaml
actions:
  - action: jdbcExecute
    query: "DELETE FROM sessions WHERE expired = true"
```

### 4. onJdbcQuery Condition

Condition that checks database state before executing actions.

**Parameters:**

- `query` / `sql` (string) - SQL query
- `params` / `parameters` (map) - Named parameters
- `expression` (string) - Template expression (must return boolean)

**Example:**

```yaml
triggers:
  - trigger: alwaysTrue
conditions:
  - condition: onJdbcQuery
    query: "SELECT COUNT(*) as count FROM inventory WHERE product_id = :productId"
    params:
      productId: "{{ event.productId }}"
    expression: "{{ result[0].count > 0 }}"
actions:
  - action: logger
    message: "Product is in stock"
```

## Real-World Examples

### Daily Report Generation

```yaml
alias: daily-sales-report
triggers:
  - trigger: time
    at: "08:00"
actions:
  - action: jdbcQuery
    query: |
      SELECT 
        DATE(created_at) as date,
        COUNT(*) as order_count,
        SUM(total) as total_sales
      FROM orders
      WHERE DATE(created_at) = CURRENT_DATE - INTERVAL 1 DAY
    variable: salesData
    mode: single

  - action: sendEmail
    to: "management@example.com"
    subject: "Daily Sales Report - {{ salesData.date }}"
    body: |
      Orders: {{ salesData.order_count }}
      Total Sales: ${{ salesData.total_sales }}
```

### User Activity Monitoring

```yaml
alias: detect-suspicious-activity
triggers:
  - trigger: onJdbcQuery
    query: |
      SELECT user_id, COUNT(*) as failed_attempts
      FROM login_attempts
      WHERE success = false 
        AND attempt_time > NOW() - INTERVAL 5 MINUTE
      GROUP BY user_id
      HAVING COUNT(*) >= 5
    expression: "{{ result.size > 0 }}"
actions:
  - action: jdbcExecute
    query: "UPDATE users SET locked = true WHERE id = :userId"
    params:
      userId: "{{ result[0].user_id }}"

  - action: sendEmail
    to: "security@example.com"
    subject: "Account Locked - Suspicious Activity"
    body: "User {{ result[0].user_id }} locked after {{ result[0].failed_attempts }} failed attempts"
```

### Inventory Management

```yaml
alias: low-stock-alert
triggers:
  - trigger: onJdbcQuery
    query: |
      SELECT product_id, product_name, stock_quantity, min_quantity
      FROM inventory
      WHERE stock_quantity < min_quantity
    expression: "{{ result.size > 0 }}"
actions:
  - action: logger
    message: "Low stock alert for {{ result.size }} products"

  - action: jdbcExecute
    query: |
      INSERT INTO reorder_requests (product_id, quantity, requested_at)
      VALUES (:productId, :quantity, :now)
    params:
      productId: "{{ result[0].product_id }}"
      quantity: "{{ result[0].min_quantity * 2 }}"
      now: "{{ now }}"
```

### Data Cleanup

```yaml
alias: cleanup-old-sessions
triggers:
  - trigger: time
    cron: "0 2 * * *" # 2 AM daily
actions:
  - action: jdbcQuery
    query: "SELECT COUNT(*) as count FROM sessions WHERE last_activity < NOW() - INTERVAL 30 DAY"
    variable: expiredCount
    mode: single

  - action: logger
    message: "Cleaning up {{ expiredCount.count }} expired sessions"

  - action: jdbcExecute
    query: "DELETE FROM sessions WHERE last_activity < NOW() - INTERVAL 30 DAY"

  - action: logger
    message: "Cleanup completed"
```

## Configuration

### DataSource Setup

The module uses Spring's `NamedParameterJdbcTemplate`, which requires a configured `DataSource`:

```properties
# application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=user
spring.datasource.password=password
spring.datasource.driver-class-name=org.postgresql.Driver
```

### Custom JdbcTemplate

```java
@Configuration
public class JdbcConfig {

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(
            DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }
}
```

## Expression Context

In trigger and condition expressions, you have access to:

- `result` - Query result (List<Map<String, Object>> or Map<String, Object>)
- `event` - The EventContext

**Example:**

```yaml
expression: "{{ result.size > 0 and result[0].status == 'PENDING' }}"
expression: "{{ result[0].count > event.threshold }}"
```

## Testing

```java
@SpringBootTest
@Import(TestConfig.class)
class JdbcActionTest extends AutomationEngineTest {

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    void testJdbcQuery() {
        // Mock query result
        when(jdbcTemplate.queryForList(any(), any()))
            .thenReturn(List.of(Map.of("count", 5)));

        var yaml = """
            alias: test-jdbc
            triggers:
              - trigger: alwaysTrue
            actions:
              - action: jdbcQuery
                query: "SELECT COUNT(*) as count FROM tasks"
                variable: result
                mode: single

              - action: logger
                message: "Count: {{ result.count }}"
            """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);
        engine.publishEvent(new TimeBasedEvent(LocalTime.now()));

        verify(jdbcTemplate).queryForList(eq("SELECT COUNT(*) as count FROM tasks"), any());
    }
}
```

## Dependencies

- `automation-engine-spring` - Spring integration
- `org.springframework.boot:spring-boot-starter-jdbc` - JDBC support
- Database driver (PostgreSQL, MySQL, H2, etc.)

## See Also

- **[automation-engine-spring](../automation-engine-spring/README.md)** - Spring integration
- **[AI_PROMPT.md](../AI_PROMPT.md)** - JDBC syntax reference
