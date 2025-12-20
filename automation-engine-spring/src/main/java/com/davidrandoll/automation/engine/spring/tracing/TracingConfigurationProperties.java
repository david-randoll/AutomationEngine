package com.davidrandoll.automation.engine.spring.tracing;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for automation execution tracing.
 */
@Data
@ConfigurationProperties(prefix = "automation-engine.tracing")
public class TracingConfigurationProperties {
    
    /**
     * Enable or disable automation execution tracing.
     * Default: false
     */
    private boolean enabled = false;
    
    /**
     * Include context snapshots in trace data.
     * Default: true
     */
    private boolean includeContextSnapshots = true;
    
    /**
     * Context snapshot mode.
     * - NONE: Don't capture snapshots
     * - KEYS_ONLY: Capture only metadata keys (low overhead)
     * - FULL: Capture full key-value pairs (higher overhead)
     * Default: KEYS_ONLY
     */
    private SnapshotMode snapshotMode = SnapshotMode.KEYS_ONLY;
    
    /**
     * Clear trace data from EventContext metadata after extraction.
     * Helps prevent memory leaks for long-running contexts.
     * Default: true
     */
    private boolean clearAfterExtraction = true;
    
    /**
     * Enable trace data persistence (requires IAutomationTraceRepository bean).
     * Default: false
     */
    private boolean persistenceEnabled = false;
    
    /**
     * Snapshot mode enumeration.
     */
    public enum SnapshotMode {
        NONE,
        KEYS_ONLY,
        FULL
    }
}
