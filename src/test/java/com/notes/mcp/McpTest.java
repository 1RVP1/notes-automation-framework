package com.notes.mcp;

import com.notes.config.ConfigReader;
import com.notes.utils.BaseTest;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Epic("Notes Application")
@Feature("MCP - Model Context Protocol")
public class McpTest extends BaseTest {

    private McpSeleniumServer mcp;

    @BeforeMethod(alwaysRun = true)
    public void mcpSetup() {
        mcp = new McpSeleniumServer();
    }

    // TC-MCP-01: MCP tool router — login via UI
    @Test(description = "TC-MCP-01: MCP notes_login tool executes UI login")
    @Story("MCP - Tool: notes_login")
    @Severity(SeverityLevel.CRITICAL)
    public void testMcpUiLogin() {

        log.info("Running TC-MCP-01: MCP UI Login");

        Map<String, String> params = new HashMap<>();
        params.put("email", ConfigReader.getEmail());
        params.put("password", ConfigReader.getPassword());
        params.put("mode", "ui");

        McpToolResult result =
                mcp.executeTool(McpConfig.TOOL_LOGIN, params);

        Assert.assertTrue(
                result.isSuccess(),
                "MCP login tool should succeed."
        );

        Assert.assertEquals(
                result.get("mode"),
                "ui"
        );

        Assert.assertTrue(
                (Boolean) result.get("loginSuccess"),
                "MCP UI login should load dashboard."
        );

        log.info("TC-MCP-01 PASSED: MCP UI login tool working.");
    }

    // TC-MCP-02: MCP full workflow — login → create → get → delete
    @Test(description = "TC-MCP-02: MCP full workflow via tool chain")
    @Story("MCP - Full tool chain workflow")
    @Severity(SeverityLevel.CRITICAL)
    public void testMcpFullWorkflow() {

        log.info("Running TC-MCP-02: MCP Full Workflow");

        // Step 1: Login via MCP (UI)
        Map<String, String> loginParams = new HashMap<>();
        loginParams.put("mode", "ui");

        McpToolResult loginResult =
                mcp.executeTool(McpConfig.TOOL_LOGIN, loginParams);

        Assert.assertTrue(
                (Boolean) loginResult.get("loginSuccess"),
                "Step 1: Login must pass."
        );

        log.info("[MCP Workflow] Step 1 DONE: Login");

        // Step 2: Login via MCP (API)
        loginParams.put("mode", "api");

        mcp.executeTool(
                McpConfig.TOOL_LOGIN,
                loginParams
        );

        log.info("[MCP Workflow] Step 2 DONE: API token obtained");

        // Step 3: Create note via MCP
        String workflowTitle =
                "McpWorkflow_" + System.currentTimeMillis();

        Map<String, String> createParams = new HashMap<>();
        createParams.put("title", workflowTitle);
        createParams.put("description", "Full MCP workflow test");
        createParams.put("category", "Home");

        McpToolResult createResult =
                mcp.executeTool(
                        McpConfig.TOOL_CREATE_NOTE,
                        createParams
                );

        Assert.assertTrue(
                (Boolean) createResult.get("noteVisible"),
                "Step 3: Note must be visible."
        );

        log.info("[MCP Workflow] Step 3 DONE: Note created");

        // Step 4: Get notes and verify
        McpToolResult getResult =
                mcp.executeTool(
                        McpConfig.TOOL_GET_NOTES,
                        new HashMap<>()
                );

        List<String> titles =
                (List<String>) getResult.get("titles");

        List<String> ids =
                (List<String>) getResult.get("ids");

        Assert.assertTrue(
                titles.contains(workflowTitle),
                "Step 4: Created note must appear in GET /notes."
        );

        log.info("[MCP Workflow] Step 4 DONE: Note verified");

        // Step 5: Delete note
        int idx = titles.indexOf(workflowTitle);

        String noteId = ids.get(idx);

        Map<String, String> deleteParams = new HashMap<>();
        deleteParams.put("noteId", noteId);

        McpToolResult deleteResult =
                mcp.executeTool(
                        McpConfig.TOOL_DELETE_NOTE,
                        deleteParams
                );

        Assert.assertTrue(
                (Boolean) deleteResult.get("deleted"),
                "Step 5: Note must be deleted."
        );

        log.info("[MCP Workflow] Step 5 DONE: Note deleted");

        // Step 6: Verify deleted
        McpToolResult verifyResult =
                mcp.executeTool(
                        McpConfig.TOOL_GET_NOTES,
                        new HashMap<>()
                );

        List<String> titlesAfter =
                (List<String>) verifyResult.get("titles");

        Assert.assertFalse(
                titlesAfter != null &&
                        titlesAfter.contains(workflowTitle),
                "Step 6: Deleted note must not appear in API."
        );

        log.info("TC-MCP-02 PASSED: Full MCP workflow completed successfully.");
    }

    // TC-MCP-03: MCP unknown tool — graceful failure
    @Test(description = "TC-MCP-03: MCP gracefully handles unknown tool name")
    @Story("MCP - Error handling")
    @Severity(SeverityLevel.NORMAL)
    public void testMcpUnknownTool() {

        log.info("Running TC-MCP-03: MCP Unknown Tool");

        McpToolResult result =
                mcp.executeTool(
                        "notes_unknown_tool_xyz",
                        new HashMap<>()
                );

        Assert.assertFalse(
                result.isSuccess(),
                "Unknown MCP tool should return failure."
        );

        Assert.assertTrue(
                result.getMessage().contains("Unknown MCP tool"),
                "Failure message should indicate unknown tool."
        );

        log.info("TC-MCP-03 PASSED: Unknown tool handled gracefully.");
    }
}