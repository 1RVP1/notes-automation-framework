package com.notes.mcp;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a result returned from an MCP tool execution.
 * Wraps success/failure status, data payload, and metadata.
 */
public class McpToolResult {

    private final String toolName;
    private final boolean success;
    private final String message;
    private final Map<String, Object> data;
    private final long executionTimeMs;

    public McpToolResult(String toolName, boolean success, String message,
                         Map<String, Object> data, long executionTimeMs) {
        this.toolName        = toolName;
        this.success         = success;
        this.message         = message;
        this.data            = data != null ? data : new HashMap<>();
        this.executionTimeMs = executionTimeMs;
    }

    // ── Factory methods ───────────────────────────────────────────────

    public static McpToolResult success(String toolName, String message,
                                        Map<String, Object> data, long timeMs) {
        return new McpToolResult(toolName, true, message, data, timeMs);
    }

    public static McpToolResult failure(String toolName, String message, long timeMs) {
        return new McpToolResult(toolName, false, message, null, timeMs);
    }

    // ── Getters ───────────────────────────────────────────────────────

    public String getToolName()        { return toolName; }
    public boolean isSuccess()         { return success; }
    public String getMessage()         { return message; }
    public Map<String, Object> getData(){ return data; }
    public long getExecutionTimeMs()   { return executionTimeMs; }

    public Object get(String key) {
        return data.get(key);
    }

    @Override
    public String toString() {
        return String.format("[McpToolResult] tool=%s success=%s message='%s' timeMs=%d data=%s",
                toolName, success, message, executionTimeMs, data);
    }
}