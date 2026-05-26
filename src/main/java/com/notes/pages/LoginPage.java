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

public class LoginPage {

    private static final Logger log = LogManager.getLogger(LoginPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By EMAIL_FIELD    = By.id("email");
    private static final By PASSWORD_FIELD = By.id("password");
    private static final By LOGIN_BUTTON   = By.cssSelector("[data-testid='login-submit']");
    private static final By ERROR_MESSAGE  = By.cssSelector("[data-testid='alert-message']");

    public LoginPage() {
        this.driver = DriverManager.getDriver();
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void enterEmail(String email) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_FIELD));
        field.clear();
        field.sendKeys(email);
        log.info("Entered email: " + email);
    }

    public void enterPassword(String password) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(PASSWORD_FIELD));
        field.clear();
        field.sendKeys(password);
        log.info("Entered password.");
    }

    public void clickLogin() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(LOGIN_BUTTON));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        log.info("Clicked Login button via JS.");
    }

    public DashboardPage loginAs(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickLogin();
        return new DashboardPage();
    }

    public boolean isErrorDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE));
            return driver.findElement(ERROR_MESSAGE).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isLoginButtonEnabled() {
        return driver.findElement(LOGIN_BUTTON).isEnabled();
    }
}