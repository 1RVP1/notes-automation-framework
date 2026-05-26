package com.notes.utils;

import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Agentic self-healing driver utility.
 *
 * When a primary locator fails, it automatically tries a ranked list of
 * fallback locators and logs which one healed the interaction. This makes
 * the suite resilient to minor DOM changes without human intervention.
 */
public class SelfHealingDriver {

    private static final Logger log = LogManager.getLogger(SelfHealingDriver.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    public SelfHealingDriver() {
        this.driver = DriverManager.getDriver();
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    /**
     * Finds an element by trying each locator in order.
     * Returns the first one that resolves. Logs + attaches healing events to Allure.
     *
     * @param elementName  Human-readable name for logging (e.g. "Login Button")
     * @param locators     Ordered list of By locators (primary first, fallbacks after)
     */
    public WebElement findWithHealing(String elementName, By... locators) {
        List<By> locatorList = Arrays.asList(locators);
        for (int i = 0; i < locatorList.size(); i++) {
            By locator = locatorList.get(i);
            try {
                WebElement element = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(locator));
                if (i == 0) {
                    log.info("[SelfHeal] '{}' found with primary locator: {}", elementName, locator);
                } else {
                    String healMsg = String.format(
                            "[SelfHeal] '%s' HEALED — primary failed, used fallback #%d: %s",
                            elementName, i, locator);
                    log.warn(healMsg);
                    Allure.addAttachment("Self-Heal Event: " + elementName, healMsg);
                }
                return element;
            } catch (TimeoutException | NoSuchElementException e) {
                log.warn("[SelfHeal] '{}' locator #{} failed: {} — trying next...",
                        elementName, i, locator);
            }
        }
        String errMsg = "[SelfHeal] All locators exhausted for: " + elementName;
        log.error(errMsg);
        Allure.addAttachment("Self-Heal FAILED: " + elementName, errMsg);
        throw new NoSuchElementException(errMsg);
    }

    /**
     * Clicks an element with self-healing. Falls back to JS click if normal click fails.
     */
    public void clickWithHealing(String elementName, By... locators) {
        WebElement element = findWithHealing(elementName, locators);
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
            log.info("[SelfHeal] Clicked '{}' with normal click.", elementName);
        } catch (Exception e) {
            log.warn("[SelfHeal] Normal click failed for '{}', falling back to JS click.", elementName);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            Allure.addAttachment("Self-Heal JS Click: " + elementName,
                    "Normal click failed — used JS click instead.");
            log.info("[SelfHeal] JS click succeeded for '{}'.", elementName);
        }
    }

    /**
     * Types into a field with self-healing locator resolution.
     */
    public void typeWithHealing(String elementName, String text, By... locators) {
        WebElement element = findWithHealing(elementName, locators);
        element.clear();
        element.sendKeys(text);
        log.info("[SelfHeal] Typed into '{}': {}", elementName, text);
    }

    /**
     * Returns true if the element is visible using any of the provided locators.
     */
    public boolean isVisibleWithHealing(String elementName, By... locators) {
        for (By locator : locators) {
            try {
                new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.visibilityOfElementLocated(locator));
                log.info("[SelfHeal] '{}' is visible via: {}", elementName, locator);
                return true;
            } catch (Exception ignored) {}
        }
        log.warn("[SelfHeal] '{}' not visible with any locator.", elementName);
        return false;
    }
}