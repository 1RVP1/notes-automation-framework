package com.notes.pages;

import com.notes.utils.DriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class CreateNotePage {

    private static final Logger log = LogManager.getLogger(CreateNotePage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By TITLE_FIELD       = By.id("title");
    private static final By DESCRIPTION_FIELD = By.id("description");
    private static final By CATEGORY_DROPDOWN = By.id("category");
    private static final By SAVE_BUTTON       = By.cssSelector("[data-testid='note-submit']");
    private static final By SUCCESS_MESSAGE   = By.cssSelector(".alert-success, #note-message");

    public CreateNotePage() {
        this.driver = DriverManager.getDriver();
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void enterTitle(String title) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(TITLE_FIELD));
        field.clear();
        field.sendKeys(title);
        log.info("Entered note title: " + title);
    }

    public void enterDescription(String description) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(DESCRIPTION_FIELD));
        field.clear();
        field.sendKeys(description);
        log.info("Entered note description.");
    }

    public void selectCategory(String category) {
        WebElement dropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(CATEGORY_DROPDOWN));
        new Select(dropdown).selectByVisibleText(category);
        log.info("Selected category: " + category);
    }

    public DashboardPage saveNote() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(SAVE_BUTTON));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        log.info("Clicked Save Note.");
        // Wait for dashboard to load after save
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='add-new-note']")));
        return new DashboardPage();
    }

    public DashboardPage createNote(String title, String description, String category) {
        enterTitle(title);
        enterDescription(description);
        selectCategory(category);
        return saveNote();
    }

    public String getSuccessMessage() {
        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(SUCCESS_MESSAGE));
        return msg.getText();
    }
}