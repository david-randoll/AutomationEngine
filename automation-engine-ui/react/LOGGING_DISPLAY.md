# React Playground - Structured Logging Display

## Overview

The React playground displays structured logs with color-coded logging levels through convenient modal dialogs, keeping the interface clean while providing easy access to detailed logging information.

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

Created a reusable component (`LogsViewer.tsx`) that displays logs with:

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
- **Scrollable**: Adaptive height with scroll for many logs
- **Count Display**: Shows total number of logs in header

### 4. Trace-Level Logs Modal

Access all logs from the entire execution via a modal:
- **Button**: "View Logs (N)" button in the header next to "Copy Trace"
- **Location**: Top header bar of the playground
- **Icon**: List icon (FaListAlt)
- **Shows**: Count of total logs
- **Modal**: Full-screen modal (90vw × 90vh) with all execution logs
- **Only visible when**: Trace has logs

### 5. Component-Level Logs Modal

View logs from individual components via a modal:
- **Button**: "Logs (N)" button in the detail panel
- **Location**: In the diff controls section, left side
- **Icon**: List icon (FaListAlt)
- **Shows**: Count of component logs
- **Modal**: Full-screen modal (90vw × 90vh) with component-specific logs
- **Only visible when**: Selected component has logs

## Visual Layout

```
┌─────────────────────────────────────────────────────────────┐
│ Playground Header                                           │
│ [Execute/View Trace tabs]  [View Logs (42)] [Copy Trace]   │
├─────────────────────────────────────────────────────────────┤
│ Left Panel          │ React Flow Trace Graph                │
│ (Automation/Input)  │                                       │
│                     │                                       │
│                     │  [Interactive node graph]            │
│                     │                                       │
└─────────────────────┴───────────────────────────────────────┘
                      │ Detail Panel                          │
                      │ [Logs (8)]                            │
                      │ [Result/Event/Context] [Fullscreen]  │
                      │ Diff Viewer                          │
                      └───────────────────────────────────────┘

When "View Logs" clicked:
┌─────────────────────────────────────────────────────────────┐
│ All Execution Logs                                      [X] │
├─────────────────────────────────────────────────────────────┤
│  ● INFO    14:23:45.123  Starting automation execution     │
│  ● DEBUG   14:23:45.156  Resolving variables               │
│  ● ERROR   14:23:45.234  Database connection failed        │
│  ● WARN    14:23:45.267  Retrying connection...            │
│  ● INFO    14:23:45.523  Automation completed              │
│                                                             │
│  [Scrollable, color-coded, expandable arguments]           │
└─────────────────────────────────────────────────────────────┘
```

## Usage Example

When an automation executes with logging:

1. **View All Logs**: Click "View Logs (N)" button in the header to see all logs from the entire execution
2. **View Component Logs**: Click any node in the graph, then click "Logs (N)" in the detail panel to see only that component's logs
3. **Color Coding**: Instantly identify ERROR (red), WARN (yellow), INFO (blue), DEBUG (purple), TRACE (gray)
4. **Details**: Expand "Arguments" sections to see the raw arguments passed to the logger
5. **Close**: Click outside the modal or the X button to close

## Technical Details

### Button Visibility Logic

**Trace-Level Button** (PlaygroundPage.tsx):
```typescript
{trace.logs && trace.logs.length > 0 && (
    <Button onClick={() => setLogsModalOpen(true)}>
        View Logs ({trace.logs.length})
    </Button>
)}
```

**Component-Level Button** (TraceDetailPanel.tsx):
```typescript
{entry.logs && entry.logs.length > 0 && (
    <Button onClick={() => setLogsModalOpen(true)}>
        Logs ({entry.logs.length})
    </Button>
)}
```

### Modal Configuration

Both modals use the same configuration for consistency:
```typescript
<Dialog open={logsModalOpen} onOpenChange={setLogsModalOpen}>
    <DialogContent className="max-w-[90vw] w-[90vw] max-h-[90vh] h-[90vh]">
        <LogsViewer 
            logs={logs} 
            title=""
            maxHeight="100%"
            className="h-full"
        />
    </DialogContent>
</Dialog>
```

## Files Modified

1. **types/trace.ts**: Added `LogEntry` interface and `logs` fields
2. **playground/LogsViewer.tsx**: Log display component with color coding
3. **playground/PlaygroundPage.tsx**: Added trace-level logs button and modal
4. **playground/TraceDetailPanel.tsx**: Added component-level logs button and modal
5. **playground/TraceCanvas.tsx**: Removed inline logs display
6. **playground/index.ts**: Exported LogsViewer component

## Benefits

- **Clean Interface**: Logs don't clutter the main interface
- **On-Demand Access**: View logs only when needed
- **Hierarchical Access**: Separate buttons for trace-level and component-level logs
- **Quick Identification**: Color-coded levels help spot errors/warnings instantly
- **Full Context**: Access both formatted message and original arguments
- **Temporal Analysis**: Timestamps enable tracking execution order
- **Large Display**: Full-screen modals provide ample space for log analysis

## Future Enhancements

Potential improvements:
- Filter logs by level (show only ERROR/WARN)
- Search/filter logs by content
- Export logs to file
- Copy individual log entries
- Log level statistics/counts in modal header
