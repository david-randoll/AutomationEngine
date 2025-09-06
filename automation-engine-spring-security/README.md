# Automation Engine Spring Security Module

This module provides Spring Security integration for the Automation Engine, enabling authentication and authorization checks in automation workflows.

## Features

### Conditions

#### IsAuthenticatedCondition
Checks if the current user is authenticated (not anonymous).

```yaml
conditions:
  - type: is_authenticated
    alias: "user-logged-in"
    description: "Check if user is authenticated"
```

#### IsAnonymousCondition  
Checks if the current user is anonymous (not authenticated).

```yaml
conditions:
  - type: is_anonymous
    alias: "user-not-logged-in" 
    description: "Check if user is anonymous"
```

#### CurrentUserUsernameCondition
Checks if the current authenticated user has a specific username.

```yaml
conditions:
  - type: current_user_username
    alias: "is-admin-user"
    description: "Check if current user is admin"
    expectedUsername: "admin"
```

#### CurrentUserHasRoleCondition
Checks if the current authenticated user has specific roles.

```yaml
conditions:
  - type: current_user_has_role
    alias: "user-has-admin-role"
    description: "Check if user has admin role"
    requiredRoles: ["ADMIN"]
    requireAllRoles: false  # true = must have ALL roles, false = must have ANY role
```

Multiple roles example:
```yaml
conditions:
  - type: current_user_has_role
    alias: "user-has-multiple-roles"
    description: "Check if user has both admin and moderator roles"
    requiredRoles: ["ADMIN", "MODERATOR"]
    requireAllRoles: true
```

## Configuration

Add to your `application.yml`:

```yaml
automation:
  engine:
    spring:
      security:
        enabled: true  # default: true
```

## Dependencies

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.davidrandoll</groupId>
    <artifactId>automation-engine-spring-security</artifactId>
    <version>1.3.1</version>
</dependency>
```

## Requirements

- Java 21+
- Spring Boot 3.5+
- Spring Security 6.2+
- Automation Engine Core 1.3.1+

## Usage Example

```yaml
automations:
  - name: "admin-only-workflow"
    triggers:
      - type: http_request
        path: "/admin/action"
    conditions:
      - type: is_authenticated
        alias: "user-authenticated"
      - type: current_user_has_role
        alias: "user-is-admin"
        requiredRoles: ["ADMIN"]
    actions:
      - type: log
        message: "Admin action executed by user: ${authentication.name}"
```

## Role Handling

The module automatically handles role normalization:
- Roles with or without "ROLE_" prefix are supported
- "ADMIN" and "ROLE_ADMIN" are treated as equivalent
- Case-sensitive matching is used

## Spring Security Integration

This module integrates with Spring Security's `SecurityContextHolder` to access:
- Current authentication state
- User principal information  
- Granted authorities/roles

Make sure Spring Security is properly configured in your application.