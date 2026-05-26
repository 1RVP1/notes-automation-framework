package com.notes.mcp;

import com.notes.config.ConfigReader;
import com.notes.utils.ApiHelper;
import com.notes.utils.DriverManager;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP Selenium Server — core MCP implementation layer.
 *
 * This class implements the Model Context Protocol pattern for Selenium.
 * Each public method is an MCP "tool" — a named, self-describing capability
 * that can be invoked by name with a parameters map, just like an MCP server
 * would expose tools to an AI agent.
 *
 * Tool invocation flow:
 *   Agent calls executeTool(toolName, params)
 *     → McpSeleniumServer routes to the correct tool method
 *     → Tool executes and returns McpToolResult
 *     → Result is logged + attached to Allure
 *
 * This enables AI-assisted test generation and execution where an agent
 * can call tools like "notes_login", "notes_create_note" without knowing
 * the underlying Selenium implementation.
 */
public class McpSeleniumServer {

    private static final Logger log = LogManager.getLogger(McpSeleniumServer.class);
    private WebDriver driver;
    private WebDriverWait wait;

    public McpSeleniumServer() {
        this.driver = DriverManager.getDriver();
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    // ── MCP Tool Router ───────────────────────────────────────────────

    /**
     * Central tool dispatcher — routes tool name to implementation.
     * This is the MCP "call_tool" handler.
     */
    public McpToolResult executeTool(String toolName, Map<String, String> params) {
        log.info("[MCP] Executing tool: '{}' with params: {}", toolName, params);
        long start = System.currentTimeMillis();

        try {
            McpToolResult result = switch (toolName) {
                case McpConfig.TOOL_LOGIN         -> toolLogin(params);
                case McpConfig.TOOL_CREATE_NOTE   -> toolCreateNote(params);
                case McpConfig.TOOL_GET_NOTES     -> toolGetNotes(params);
                case McpConfig.TOOL_DELETE_NOTE   -> toolDeleteNote(params);
                case McpConfig.TOOL_FIND_ELEMENT  -> toolFindElement(params);
                case McpConfig.TOOL_CLICK_ELEMENT -> toolClickElement(params);
                case McpConfig.TOOL_TYPE_TEXT     -> toolTypeText(params);
                case McpConfig.TOOL_GET_PAGE_STATE-> toolGetPageState(params);
                default -> McpToolResult.failure(toolName,
                        "Unknown MCP tool: " + toolName,
                        System.currentTimeMillis() - start);
            };

            long elapsed = System.currentTimeMillis() - start;
            log.info("[MCP] Tool '{}' completed in {}ms — success={}",
                    toolName, elapsed, result.isSuccess());
            Allure.addAttachment("MCP Tool: " + toolName, result.toString());
            return result;

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[MCP] Tool '{}' threw exception: {}", toolName, e.getMessage());
            Allure.addAttachment("MCP Tool FAILED: " + toolName, e.getMessage());
            return McpToolResult.failure(toolName, e.getMessage(), elapsed);
        }
    }

    // ── MCP Tool Implementations ──────────────────────────────────────

    /**
     * Tool: notes_login
     * Params: email, password
     * Returns: token (if API login), loginSuccess (if UI login)
     */
    private McpToolResult toolLogin(Map<String, String> params) {
        long start = System.currentTimeMillis();
        String email    = params.getOrDefault("email", ConfigReader.getEmail());
        String password = params.getOrDefault("password", ConfigReader.getPassword());
        String mode     = params.getOrDefault("mode", "ui");

        if ("api".equals(mode)) {
            String token = ApiHelper.loginAndGetToken(email, password);
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("mode", "api");
            log.info("[MCP] notes_login (API) — token obtained: {}", token != null);
            return McpToolResult.success(McpConfig.TOOL_LOGIN,
                    "API login successful", data, System.currentTimeMillis() - start);
        }

        // UI login
        driver.get(ConfigReader.getBaseUrl() + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")))
                .sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].click();",
                wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("[data-testid='login-submit']"))));

        boolean dashboardLoaded = false;
        try {
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("[data-testid='add-new-note']")));
            dashboardLoaded = true;
        } catch (TimeoutException ignored) {}

        Map<String, Object> data = new HashMap<>();
        data.put("loginSuccess", dashboardLoaded);
        data.put("mode", "ui");
        log.info("[MCP] notes_login (UI) — dashboard loaded: {}", dashboardLoaded);
        return McpToolResult.success(McpConfig.TOOL_LOGIN,
                dashboardLoaded ? "UI login successful" : "UI login failed",
                data, System.currentTimeMillis() - start);
    }

    /**
     * Tool: notes_create_note
     * Params: title, description, category
     * Returns: noteVisible
     */
    private McpToolResult toolCreateNote(Map<String, String> params) {
        long start = System.currentTimeMillis();
        String title       = params.getOrDefault("title", "MCP Note");
        String description = params.getOrDefault("description", "Created via MCP tool");
        String category    = params.getOrDefault("category", "Home");

        // Click Add Note
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();",
                wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("[data-testid='add-new-note']"))));

        // Fill form
        WebElement titleField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("title")));
        titleField.clear();
        titleField.sendKeys(title);

        WebElement descField = driver.findElement(By.id("description"));
        descField.clear();
        descField.sendKeys(description);

        new Select(driver.findElement(By.id("category")))
                .selectByVisibleText(category);

        // Save
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();",
                wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("[data-testid='note-submit']"))));

        // Wait for dashboard
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='add-new-note']")));

        boolean noteVisible = false;
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//*[contains(text(),'" + title + "')]")));
            noteVisible = true;
        } catch (TimeoutException ignored) {}

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("category", category);
        data.put("noteVisible", noteVisible);
        log.info("[MCP] notes_create_note — title='{}' visible={}", title, noteVisible);
        return McpToolResult.success(McpConfig.TOOL_CREATE_NOTE,
                "Note creation executed", data, System.currentTimeMillis() - start);
    }

    /**
     * Tool: notes_get_notes
     * Params: none (uses current auth token)
     * Returns: count, titles list
     */
    private McpToolResult toolGetNotes(Map<String, String> params) {
        long start = System.currentTimeMillis();
        io.restassured.response.Response response = ApiHelper.getAllNotes();
        List<String> titles = response.jsonPath().getList("data.title");
        List<String> ids    = response.jsonPath().getList("data.id");

        Map<String, Object> data = new HashMap<>();
        data.put("statusCode", response.getStatusCode());
        data.put("count", titles != null ? titles.size() : 0);
        data.put("titles", titles);
        data.put("ids", ids);
        log.info("[MCP] notes_get_notes — count={}", data.get("count"));
        return McpToolResult.success(McpConfig.TOOL_GET_NOTES,
                "GET /notes executed", data, System.currentTimeMillis() - start);
    }

    /**
     * Tool: notes_delete_note
     * Params: noteId
     * Returns: statusCode
     */
    private McpToolResult toolDeleteNote(Map<String, String> params) {
        long start = System.currentTimeMillis();
        String noteId = params.get("noteId");
        if (noteId == null || noteId.isEmpty()) {
            return McpToolResult.failure(McpConfig.TOOL_DELETE_NOTE,
                    "noteId param is required", System.currentTimeMillis() - start);
        }
        io.restassured.response.Response response = ApiHelper.deleteNoteApi(noteId);
        Map<String, Object> data = new HashMap<>();
        data.put("noteId", noteId);
        data.put("statusCode", response.getStatusCode());
        data.put("deleted", response.getStatusCode() == 200);
        log.info("[MCP] notes_delete_note — id={} status={}", noteId, response.getStatusCode());
        return McpToolResult.success(McpConfig.TOOL_DELETE_NOTE,
                "DELETE /notes/" + noteId + " executed", data, System.currentTimeMillis() - start);
    }

    /**
     * Tool: notes_find_element
     * Params: strategy (id/css/xpath), value
     * Returns: found, text, tagName
     */
    private McpToolResult toolFindElement(Map<String, String> params) {
        long start = System.currentTimeMillis();
        String strategy = params.getOrDefault("strategy", "css");
        String value    = params.get("value");

        By locator = switch (strategy) {
            case "id"    -> By.id(value);
            case "xpath" -> By.xpath(value);
            default      -> By.cssSelector(value);
        };

        Map<String, Object> data = new HashMap<>();
        try {
            WebElement el = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            data.put("found", true);
            data.put("text", el.getText());
            data.put("tagName", el.getTagName());
            data.put("displayed", el.isDisplayed());
            data.put("enabled", el.isEnabled());
            log.info("[MCP] notes_find_element — strategy={} value='{}' found=true", strategy, value);
        } catch (Exception e) {
            data.put("found", false);
            log.warn("[MCP] notes_find_element — not found: {}", value);
        }
        return McpToolResult.success(McpConfig.TOOL_FIND_ELEMENT,
                "Element search completed", data, System.currentTimeMillis() - start);
    }

    /**
     * Tool: notes_click_element
     * Params: strategy, value
     * Returns: clicked
     */
    private McpToolResult toolClickElement(Map<String, String> params) {
        long start = System.currentTimeMillis();
        String strategy = params.getOrDefault("strategy", "css");
        String value    = params.get("value");
        By locator = switch (strategy) {
            case "id"    -> By.id(value);
            case "xpath" -> By.xpath(value);
            default      -> By.cssSelector(value);
        };

        Map<String, Object> data = new HashMap<>();
        try {
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
            try {
                el.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
            }
            data.put("clicked", true);
            log.info("[MCP] notes_click_element — clicked: {}", value);
        } catch (Exception e) {
            data.put("clicked", false);
            log.warn("[MCP] notes_click_element — failed: {}", e.getMessage());
        }
        return McpToolResult.success(McpConfig.TOOL_CLICK_ELEMENT,
                "Click executed", data, System.currentTimeMillis() - start);
    }

    /**
     * Tool: notes_type_text
     * Params: strategy, value, text
     * Returns: typed
     */
    private McpToolResult toolTypeText(Map<String, String> params) {
        long start = System.currentTimeMillis();
        String strategy = params.getOrDefault("strategy", "id");
        String value    = params.get("value");
        String text     = params.getOrDefault("text", "");
        By locator = switch (strategy) {
            case "css"   -> By.cssSelector(value);
            case "xpath" -> By.xpath(value);
            default      -> By.id(value);
        };

        Map<String, Object> data = new HashMap<>();
        try {
            WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            el.clear();
            el.sendKeys(text);
            data.put("typed", true);
            data.put("text", text);
            log.info("[MCP] notes_type_text — typed '{}' into: {}", text, value);
        } catch (Exception e) {
            data.put("typed", false);
            log.warn("[MCP] notes_type_text — failed: {}", e.getMessage());
        }
        return McpToolResult.success(McpConfig.TOOL_TYPE_TEXT,
                "Type executed", data, System.currentTimeMillis() - start);
    }

    /**
     * Tool: notes_get_page_state
     * Params: none
     * Returns: url, title, readyState, noteCount
     */
    private McpToolResult toolGetPageState(Map<String, String> params) {
        long start = System.currentTimeMillis();
        Map<String, Object> data = new HashMap<>();
        data.put("url", driver.getCurrentUrl());
        data.put("title", driver.getTitle());
        data.put("readyState",
                ((JavascriptExecutor) driver).executeScript("return document.readyState"));
        List<WebElement> notes = driver.findElements(
                By.cssSelector(".card, .note-item"));
        data.put("noteCount", notes.size());
        log.info("[MCP] notes_get_page_state — url={} noteCount={}", data.get("url"), data.get("noteCount"));
        return McpToolResult.success(McpConfig.TOOL_GET_PAGE_STATE,
                "Page state captured", data, System.currentTimeMillis() - start);
    }
}