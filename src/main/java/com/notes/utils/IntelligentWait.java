package com.notes.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Intelligent waiting utility for stable UI automation.
 *
 * Features:
 * - Page readiness checks using document.readyState
 * - Stable element synchronization
 * - Text appearance/disappearance waits
 * - Fluent wait handling with retry polling
 */
public class IntelligentWait {

    private static final Logger log = LogManager.getLogger(IntelligentWait.class);
    private final WebDriver driver;
    private final JavascriptExecutor js;

    public IntelligentWait() {
        this.driver = DriverManager.getDriver();
        this.js     = (JavascriptExecutor) driver;
    }

    /**
     * Waits for document.readyState == 'complete'.
     * Safe to call after any navigation or page action.
     */
    public void waitForPageReady(int timeoutSeconds) {
        new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .until((ExpectedCondition<Boolean>) d ->
                        js.executeScript("return document.readyState").equals("complete"));
        log.info("[IntelligentWait] Page ready (readyState=complete).");
    }

    /**
     * Waits until the element is visible, enabled, and its position on screen
     * has stopped moving (stable for 300ms). Prevents clicks on elements mid-animation.
     */
    public WebElement waitForStableElement(By locator, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));

        // Poll until position stops changing
        new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(Duration.ofMillis(300))
                .ignoring(StaleElementReferenceException.class)
                .until(d -> {
                    Point before = element.getLocation();
                    try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                    Point after = element.getLocation();
                    boolean stable = before.equals(after);
                    if (!stable) log.debug("[IntelligentWait] Element still moving: {}", locator);
                    return stable;
                });

        log.info("[IntelligentWait] Element stable and ready: {}", locator);
        return element;
    }

    /**
     * Waits until a specific text disappears from the DOM — useful for
     * confirming note deletion is reflected in the UI.
     */
    public void waitForTextDisappear(String text, int timeoutSeconds) {
        By locator = By.xpath("//*[contains(text(),'" + text + "')]");
        new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .until(ExpectedConditions.invisibilityOfElementLocated(locator));
        log.info("[IntelligentWait] Text disappeared from DOM: '{}'", text);
    }

    /**
     * Waits until a specific text appears anywhere in the DOM.
     */
    public void waitForTextAppear(String text, int timeoutSeconds) {
        By locator = By.xpath("//*[contains(text(),'" + text + "')]");
        new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .until(ExpectedConditions.presenceOfElementLocated(locator));
        log.info("[IntelligentWait] Text appeared in DOM: '{}'", text);
    }

}