# 📝 Todo App with Dual CRUD Approaches

This project demonstrates two ways to manage a Todo application:

1. **Traditional REST API** (Manual CRUD)
2. **Dynamic Automation Engine** (Powered by YAML or JSON)

Each step showcases how we can achieve basic functionality using the traditional method first, and then how to
accomplish the same goal — often more flexibly — using the automation engine.

---

# Step 1: Manual CRUD API (Baseline)

This is the traditional way of managing todos using Spring Boot's `@RestController` and service classes. All endpoints
follow standard REST conventions.

### ✅ Manual API Endpoints

Base URL: `/api/todos/manual`

| Method | Endpoint         | Description                   |
|--------|------------------|-------------------------------|
| POST   | `/`              | Create a new todo             |
| GET    | `/`              | Get all todos                 |
| GET    | `/{id}`          | Get a todo by ID              |
| PUT    | `/{id}/title`    | Update a todo's title         |
| PUT    | `/{id}/status`   | Change the todo's status      |
| PUT    | `/{id}/assign`   | Assign the todo to a user     |
| PUT    | `/{id}/unassign` | Unassign the todo from a user |
| DELETE | `/{id}`          | Delete the todo               |

---

### 🧩 Example Usage

#### Create a Todo

```http
POST /api/todos/manual
Content-Type: application/x-www-form-urlencoded

title=Buy groceries&statusCode=OPEN
```

#### Update Title

```http
PUT /api/todos/manual/1/title
Content-Type: application/x-www-form-urlencoded

title=Buy milk
```

---

## 💡 What Happens When Requirements Change?

Imagine the business adds a new requirement:

> 📨 "Send an email notification after a todo is created and assigned to someone."

### ❓ How do we handle this with the traditional approach?

There are a couple of common ways to do this in Spring Boot:

---

### 🛠 Option 1: Add Logic Directly in `TodoService`

One way is to inject an EmailService into TodoService and call it right after assignment:

```java
if (todo.getAssigneeUsername() != null) {
    emailService.sendAssignmentEmail(todo.getAssigneeUsername(), todo.getTitle());
}
```
✅ Pros:
- Simple and direct
- Easy to implement in small projects

❌ Cons:
- Business logic gets tightly coupled to service logic
- Difficult to maintain as features grow
- Failures in email sending could block todo creation

### 🧵 Option 2: Publish an Event
A better approach is to decouple the email logic using an event-driven architecture:
```java
applicationEventPublisher.publishEvent(new TodoAssignedEvent(todo));
```
Then, create an event listener to handle the email sending:

```java
@EventListener
public void handle(TodoAssignedEvent event) {
    emailService.sendAssignmentEmail(event.getAssigneeUsername(), event.getTitle());
}
```

✅ Pros:
- Keeps TodoService focused on core logic
- Easier to test and evolve 
- Supports async and retry logic

❌ Cons:
- Requires more infrastructure (Spring events, RabbitMQ, etc.)
- Harder to trace the full execution flow