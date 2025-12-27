# React Playground - Structured Logging Display

## Overview

The React playground now displays structured logs with color-coded logging levels for both individual components and the entire trace execution.

## Features Implemented

### 1. LogEntry Type Definition

Added `LogEntry` interface to match the Java backend structure:

```typescript
export interface LogEntry {
  message?: string;              // Log message template
  arguments?: unknown[];         // Arguments passed to logger
  formattedMessage?: string;     // Fully formatted message
  timestamp?: string;            // ISO timestamp string
}
```

### 2. Updated Trace Types

- Added `logs?: LogEntry[]` field to `BaseTraceEntry` - logs for each component (variable, trigger, condition, action, result)
- Added `logs?: LogEntry[]` field to `ExecutionTrace` - aggregated logs from entire execution

### 3. LogsViewer Component

Created a new component (`LogsViewer.tsx`) that displays logs with:

#### Color-Coded Log Levels

The component automatically detects log levels from the formatted message and applies appropriate colors:

- **ERROR** - Red background (`bg-red-50`, `text-red-700`)
- **WARN** - Yellow background (`bg-yellow-50`, `text-yellow-700`)
- **INFO** - Blue background (`bg-blue-50`, `text-blue-700`)
- **DEBUG** - Purple background (`bg-purple-50`, `text-purple-700`)
- **TRACE** - Gray background (`bg-gray-50`, `text-gray-600`)

#### Features

- **Timestamps**: Shows formatted timestamps (HH:mm:ss.SSS)
- **Log Levels**: Bold level indicator with colored dot icon
- **Message Display**: Full formatted message with word wrapping
- **Arguments**: Expandable details section showing arguments array in JSON format
- **Scrollable**: Max height with scroll for many logs
- **Count Display**: Shows total number of logs in header

### 4. Component-Level Logs

Each trace entry now displays its own logs in the `TraceDetailPanel`:

- Appears above the diff viewer
- Shows logs captured during that specific component's execution
- Title: "Component Logs"
- Max height: 200px with scrolling

### 5. Trace-Level Logs

The entire execution's logs are displayed at the top of `TraceCanvas`:

- Shows all aggregated logs from all components
- Appears above the React Flow graph
- Title: "All Execution Logs"
- Max height: 150px with scrolling
- Only visible if logs are present

## Visual Layout

```
┌─────────────────────────────────────────────────────────┐
│ Playground Header (Execute / View Trace tabs)          │
├─────────────────────────────────────────────────────────┤
│ Left Panel          │ Center/Right Panel                │
│ (Automation/Input)  │                                   │
│                     │ ┌───────────────────────────────┐ │
│                     │ │ All Execution Logs (150px)    │ │
│                     │ │ - Color coded by level        │ │
│                     │ │ - Timestamps & messages       │ │
│                     │ └───────────────────────────────┘ │
│                     │ ┌───────────────────────────────┐ │
│                     │ │                               │ │
│                     │ │ React Flow Trace Graph        │ │
│                     │ │                               │ │
│                     │ └───────────────────────────────┘ │
└─────────────────────┴───────────────────────────────────┘
│ Detail Panel                                            │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Component Logs (200px)                              │ │
│ │ - Logs from selected node                           │ │
│ │ - Color coded by level                              │ │
│ └─────────────────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Diff Viewer (Event/Context/Result)                  │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

## Usage Example

When an automation executes with logging:

1. **Trace-Level View**: The top section shows ALL logs from the entire execution in chronological order
2. **Component-Level View**: Click any node in the graph to see only logs from that specific component
3. **Color Coding**: Instantly identify ERROR (red), WARN (yellow), INFO (blue), DEBUG (purple), TRACE (gray)
4. **Details**: Click "Arguments" to expand and see the raw arguments passed to the logger

## Technical Details

### Log Level Detection

```typescript
function detectLogLevel(message: string): "ERROR" | "WARN" | "INFO" | "DEBUG" | "TRACE" {
    const upperMsg = message.toUpperCase();
    if (upperMsg.includes("ERROR") || upperMsg.includes("SEVERE")) return "ERROR";
    if (upperMsg.includes("WARN") || upperMsg.includes("WARNING")) return "WARN";
    if (upperMsg.includes("DEBUG")) return "DEBUG";
    if (upperMsg.includes("TRACE")) return "TRACE";
    return "INFO";
}
```

### Timestamp Formatting

```typescript
function formatTimestamp(timestamp?: string): string {
    const date = new Date(timestamp);
    return date.toLocaleTimeString("en-US", { 
        hour12: false, 
        hour: "2-digit", 
        minute: "2-digit", 
        second: "2-digit",
        fractionalSecondDigits: 3
    });
}
```

## Files Modified

1. **types/trace.ts**: Added `LogEntry` interface and `logs` fields
2. **playground/LogsViewer.tsx**: NEW - Log display component with color coding
3. **playground/TraceDetailPanel.tsx**: Added component-level logs section
4. **playground/TraceCanvas.tsx**: Added trace-level logs section
5. **playground/index.ts**: Exported LogsViewer component

## Benefits

- **Quick Debugging**: Color-coded levels help identify errors/warnings at a glance
- **Hierarchical View**: See logs at both trace and component levels
- **Full Context**: Access both formatted message and original arguments
- **Temporal Analysis**: Timestamps enable tracking execution order
- **Clean UI**: Scrollable sections prevent overwhelming the interface

## Future Enhancements

Potential improvements:
- Filter logs by level (show only ERROR/WARN)
- Search/filter logs by content
- Export logs to file
- Real-time log streaming during execution
- Log level statistics/counts
