package com.davidrandoll.automation.engine.spring.tracing;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a node in the automation execution tree.
 * Each node can have children, forming a hierarchical structure for UI
 * rendering.
 */
@Data
@Builder
public class ExecutionNode {

    /**
     * Type of this execution node (e.g., "automation", "phase", "variable",
     * "trigger", etc.)
     */
    private String type;

    /**
     * Name/alias of this node
     */
    private String name;

    /**
     * Execution status: "executed", "skipped", "activated", "satisfied", "failed",
     * etc.
     */
    private String status;

    /**
     * Start time in nanoseconds (optional)
     */
    private Long startTimeNanos;

    /**
     * End time in nanoseconds (optional)
     */
    private Long endTimeNanos;

    /**
     * Duration in nanoseconds (optional)
     */
    private Long durationNanos;

    /**
     * Duration in milliseconds (optional, computed from durationNanos)
     */
    private Double durationMillis;

    /**
     * Additional metadata specific to this node
     */
    @Builder.Default
    private Map<String, Object> metadata = new LinkedHashMap<>();

    /**
     * Child nodes in execution order
     */
    @Builder.Default
    private List<ExecutionNode> children = new ArrayList<>();

    /**
     * Convert this execution node to a map for JSON serialization.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("type", type);
        map.put("name", name);
        map.put("status", status);

        if (startTimeNanos != null) {
            map.put("startTimeNanos", startTimeNanos);
        }

        if (endTimeNanos != null) {
            map.put("endTimeNanos", endTimeNanos);
        }

        if (durationNanos != null) {
            map.put("durationNanos", durationNanos);
        }

        if (durationMillis != null) {
            map.put("durationMillis", durationMillis);
        }

        if (metadata != null && !metadata.isEmpty()) {
            map.put("metadata", metadata);
        }

        if (children != null && !children.isEmpty()) {
            List<Map<String, Object>> childMaps = new ArrayList<>();
            for (ExecutionNode child : children) {
                childMaps.add(child.toMap());
            }
            map.put("children", childMaps);
        }

        return map;
    }

    /**
     * Add a child node to this node.
     */
    public void addChild(ExecutionNode child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    /**
     * Check if this node has children.
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }
}
