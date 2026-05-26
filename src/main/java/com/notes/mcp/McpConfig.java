package com.notes.mcp;

/**
 * MCP (Model Context Protocol) Configuration.
 * Holds constants for MCP server connection and capability definitions.
 */
public class McpConfig {

    public static final String MCP_SERVER_NAME    = "notes-selenium-mcp";
    public static final String MCP_SERVER_VERSION = "1.0.0";
    public static final String MCP_BASE_URL       = "https://practice.expandtesting.com/notes/app";
    public static final String MCP_API_BASE_URL   = "https://practice.expandtesting.com/notes/api";

    // MCP Tool names — each maps to a capability this server exposes
    public static final String TOOL_LOGIN          = "notes_login";
    public static final String TOOL_CREATE_NOTE    = "notes_create_note";
    public static final String TOOL_GET_NOTES      = "notes_get_notes";
    public static final String TOOL_DELETE_NOTE    = "notes_delete_note";
    public static final String TOOL_FIND_ELEMENT   = "notes_find_element";
    public static final String TOOL_CLICK_ELEMENT  = "notes_click_element";
    public static final String TOOL_TYPE_TEXT      = "notes_type_text";
    public static final String TOOL_GET_PAGE_STATE = "notes_get_page_state";

    private McpConfig() {}
}