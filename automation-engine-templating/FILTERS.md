# New Pebble Template Filters

This document describes the new custom filters added to the AutomationEngine templating module.

## Filter Summary

### 1. `json` - Convert to JSON
**Purpose:** Converts any object to its JSON string representation.

**Use Cases:**
- Logging structured data
- Sending HTTP requests with JSON payloads
- Storing complex objects as strings
- Debugging template variables

**Usage:**
```yaml
# Basic usage
message: "{{ event.user | json }}"

# Pretty print
body: "{{ event | json(pretty=true) }}"

# In HTTP action
actions:
  - action: sendHttpRequest
    url: "https://api.example.com/webhook"
    body: "{{ event | json }}"
```

**Arguments:**
- `pretty` (boolean, optional): Format with indentation and newlines. Default: `false`

---

### 2. `fromJson` - Parse JSON
**Purpose:** Parses a JSON string into an object that can be accessed in templates.

**Use Cases:**
- Processing JSON responses from HTTP actions
- Parsing stored JSON configuration strings
- Working with JSON data from database queries

**Usage:**
```yaml
# Parse and access nested properties
variables:
  - apiResponse: "{{ httpResponse.body | fromJson }}"
  - userName: "{{ apiResponse.user.name }}"

# Parse array
- items: "{{ jsonArrayString | fromJson }}"
- firstItem: "{{ items[0] }}"
```

---

### 3. `date_format` - Format Dates
**Purpose:** Formats date/time objects to strings using customizable patterns.

**Supported Types:** `LocalDate`, `LocalDateTime`, `Instant`, `Date`, parseable date strings

**Use Cases:**
- Formatting timestamps for display
- Creating date-based file names
- Formatting dates for database queries
- HTTP date headers

**Usage:**
```yaml
# Default format (yyyy-MM-dd HH:mm:ss)
created: "{{ event.createdAt | date_format }}"

# Custom format
date: "{{ event.timestamp | date_format('MMM dd, yyyy') }}"

# ISO format
isoDate: "{{ event.date | date_format('yyyy-MM-dd\'T\'HH:mm:ss') }}"

# Time only
time: "{{ now | date_format('HH:mm:ss') }}"
```

**Arguments:**
- `pattern` (string, optional): Java DateTimeFormatter pattern. Default: `"yyyy-MM-dd HH:mm:ss"`

**Common Patterns:**
- `yyyy-MM-dd` → 2023-12-25
- `MM/dd/yyyy` → 12/25/2023
- `MMM dd, yyyy` → Dec 25, 2023
- `yyyy-MM-dd HH:mm:ss` → 2023-12-25 14:30:45
- `HH:mm:ss` → 14:30:45

---

### 4. `coalesce` - First Non-Null Value
**Purpose:** Returns the first non-null value from input or arguments.

**Use Cases:**
- Providing default values
- Null-safe template expressions
- Fallback chains

**Usage:**
```yaml
# Simple default
assignee: "{{ event.assignee | coalesce('unassigned') }}"

# Multiple fallbacks
owner: "{{ event.owner | coalesce(event.creator, event.submitter, 'system') }}"

# With variable
recipientEmail: "{{ user.email | coalesce(variables.defaultEmail) }}"
```

**Arguments:** Variable number of fallback values (checked in order)

**Note:** Empty strings and zero are NOT considered null.

---

### 5. `base64encode` - Encode to Base64
**Purpose:** Encodes a string to Base64 format.

**Use Cases:**
- HTTP Basic Authentication
- Encoding credentials
- Binary data transmission
- Data obfuscation (not encryption!)

**Usage:**
```yaml
# HTTP Basic Auth
actions:
  - action: sendHttpRequest
    headers:
      Authorization: "Basic {{ 'user:password' | base64encode }}"

# Encode data
encoded: "{{ sensitiveData | base64encode }}"
```

---

### 6. `base64decode` - Decode from Base64
**Purpose:** Decodes a Base64 string.

**Use Cases:**
- Decoding received credentials
- Processing base64-encoded data

**Usage:**
```yaml
# Decode credentials
credentials: "{{ encodedAuth | base64decode }}"

# Extract from header
decoded: "{{ event.authHeader | base64decode }}"
```

---

### 7. `urlEncode` - URL Encoding
**Purpose:** URL-encodes a string for safe use in URLs.

**Use Cases:**
- Building query parameters
- Encoding form data
- Creating safe URLs

**Usage:**
```yaml
# Query parameter
actions:
  - action: sendHttpRequest
    url: "https://api.example.com/search?q={{ searchTerm | urlEncode }}"

# Multiple params
url: "/api/users?name={{ name | urlEncode }}&email={{ email | urlEncode }}"
```

**Encoding Rules:**
- Spaces → `+` or `%20`
- Special chars → `%XX` (hex)
- Safe chars (A-Z, a-z, 0-9, `-`, `_`, `.`, `~`) → unchanged

---

### 8. `urlDecode` - URL Decoding
**Purpose:** Decodes a URL-encoded string.

**Use Cases:**
- Processing URL parameters
- Decoding received data

**Usage:**
```yaml
# Decode query param
searchTerm: "{{ event.queryParam | urlDecode }}"

# Decode path segment
decodedPath: "{{ event.pathParam | urlDecode }}"
```

---

## Complete Example

```yaml
alias: process-webhook-with-filters
description: Demonstrates all new filters

variables:
  - currentTime: "{{ now | date_format('yyyy-MM-dd HH:mm:ss') }}"
  - requestData: "{{ event.body | fromJson }}"
  - userName: "{{ requestData.user.name | coalesce('Guest') }}"
  - authToken: "{{ 'api_key:secret' | base64encode }}"
  - searchQuery: "{{ requestData.search | urlEncode }}"

actions:
  - action: logger
    message: "Processing request at {{ currentTime }} for user {{ userName }}"
  
  - action: sendHttpRequest
    url: "https://api.example.com/search?q={{ searchQuery }}"
    headers:
      Authorization: "Basic {{ authToken }}"
    body: "{{ requestData | json }}"
  
result:
  processedAt: "{{ currentTime }}"
  user: "{{ userName }}"
  requestJson: "{{ event | json(pretty=true) }}"
```

---

## Existing Filters (Already Available)

For reference, these filters were already in AutomationEngine:

- **`int`** - Convert to integer
- **`number_format`** - Format numbers with decimal places
- **`time_format`** - Format LocalTime objects

Plus all standard Pebble filters:
- `upper`, `lower`, `capitalize`, `title`
- `trim`, `replace`, `split`
- `join`, `first`, `last`
- `length`, `default`
- `date` (basic), `escape`, `abs`, `round`
- And many more: https://pebbletemplates.io/wiki/filter/

---

## Additional Filter Ideas (Not Yet Implemented)

These would be valuable additions in the future:

1. **`hash`** - Generate MD5/SHA-256 hashes
2. **`uuid`** - Generate UUIDs
3. **`regex`** - Extract/test regex patterns
4. **`xmlToJson` / `jsonToXml`** - Convert between formats
5. **`hmacSign`** - HMAC signature generation
6. **`encrypt` / `decrypt`** - Symmetric encryption
7. **`formatBytes`** - Human-readable byte sizes (e.g., "1.5 MB")
8. **`slugify`** - Create URL-safe slugs
9. **`truncate`** - Truncate with ellipsis
10. **`pluck`** - Extract property from array of objects

---

## Testing

All filters have comprehensive unit tests in:
```
automation-engine-templating/src/test/java/com/davidrandoll/automation/engine/templating/extensions/filters/
```

Run tests:
```bash
mvn test -pl automation-engine-templating
```

