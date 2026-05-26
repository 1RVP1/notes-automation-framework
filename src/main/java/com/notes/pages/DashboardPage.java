package com.notes.pages;

import com.notes.utils.DriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class DashboardPage {

    private static final Logger log = LogManager.getLogger(DashboardPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By ADD_NOTE_BUTTON = By.cssSelector("[data-testid='add-new-note']");
    private static final By SEARCH_FIELD    = By.cssSelector("[data-testid='search-input']");
    private static final By SEARCH_BUTTON   = By.cssSelector("[data-testid='search-btn']");
    private static final By LOGOUT_BUTTON   = By.cssSelector("[data-testid='logout']");
    private static final By NOTES_LIST      = By.cssSelector(".note-item, .card");

    public DashboardPage() {
        this.driver = DriverManager.getDriver();
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public boolean isDashboardLoaded() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(ADD_NOTE_BUTTON));
            log.info("Dashboard loaded successfully.");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void clickAddNote() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(ADD_NOTE_BUTTON));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        log.info("Clicked Add Note button.");
    }

    public void searchNote(String keyword) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(SEARCH_FIELD));
        field.clear();
        field.sendKeys(keyword);
        // Enter key instead of button click to avoid renderer timeout
        field.sendKeys(org.openqa.selenium.Keys.ENTER);
        log.info("Searched for: " + keyword);
    }

    public int getNotesCount() {
        List<WebElement> notes = driver.findElements(NOTES_LIST);
        return notes.size();
    }

    public boolean isNotePresent(String title) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//*[contains(text(),'" + title + "')]")));
            log.info("Note found in UI: " + title);
            return true;
        } catch (Exception e) {
            log.warn("Note not found in UI: " + title);
            return false;
        }
    }

    public void deleteNoteByTitle(String title) {
        WebElement deleteBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[contains(@class,'card')]//*[contains(text(),'" + title +
                        "')]/ancestor::div[contains(@class,'card')]//*[@data-testid='note-delete']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteBtn);
        log.info("Clicked Delete for note: " + title);

        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='note-delete-confirm']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmBtn);
        log.info("Confirmed deletion for note: " + title);

        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//*[contains(@class,'card')]//*[contains(text(),'" + title + "')]")));
        log.info("Note removed from UI: " + title);
    }

    public void clickLogout() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        log.info("Clicked Logout.");
    }

    public LoginPage logout() {
        clickLogout();
        return new LoginPage();
    }
}