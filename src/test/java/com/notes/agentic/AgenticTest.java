package com.notes.agentic;

import com.notes.config.ConfigReader;
import com.notes.utils.*;
import io.qameta.allure.*;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Epic("Notes Application")
@Feature("Agentic - Self-Healing + Retry + Intelligent Wait")
public class AgenticTest extends BaseTest {

    private SelfHealingDriver healingDriver;
    private IntelligentWait intelligentWait;

    // Multiple locator strategies per element
    private static final By[] EMAIL_LOCATORS = {
            By.id("email"),
            By.name("email"),
            By.cssSelector("input[type='email']"),
            By.xpath("//input[@placeholder='Email']")
    };

    private static final By[] PASSWORD_LOCATORS = {
            By.id("password"),
            By.name("password"),
            By.cssSelector("input[type='password']"),
            By.xpath("//input[@placeholder='Password']")
    };

    private static final By[] LOGIN_BTN_LOCATORS = {
            By.cssSelector("[data-testid='login-submit']"),
            By.cssSelector("button[type='submit']"),
            By.xpath("//button[contains(text(),'Login')]")
    };

    private static final By[] ADD_NOTE_LOCATORS = {
            By.cssSelector("[data-testid='add-new-note']"),
            By.xpath("//button[contains(text(),'Add')]"),
            By.cssSelector("button.btn-primary")
    };

    private static final By[] TITLE_LOCATORS = {
            By.id("title"),
            By.name("title"),
            By.cssSelector("input[placeholder*='Title']")
    };

    private static final By[] DESC_LOCATORS = {
            By.id("description"),
            By.name("description"),
            By.cssSelector("textarea[id='description']")
    };

    private static final By[] SAVE_LOCATORS = {
            By.cssSelector("[data-testid='note-submit']"),
            By.cssSelector("button[type='submit']"),
            By.xpath("//button[contains(text(),'Save')]")
    };

    @BeforeMethod(alwaysRun = true)
    public void agenticSetup() {
        healingDriver   = new SelfHealingDriver();
        intelligentWait = new IntelligentWait();
    }

    // TC-AGENT-01
    @Test(description = "TC-AGENT-01: Self-healing login flow")
    @Story("Agentic - Self-healing login")
    @Severity(SeverityLevel.CRITICAL)
    public void testSelfHealingLogin() {

        log.info("Running TC-AGENT-01");

        healingDriver.typeWithHealing(
                "Email field",
                ConfigReader.getEmail(),
                EMAIL_LOCATORS
        );

        healingDriver.typeWithHealing(
                "Password field",
                ConfigReader.getPassword(),
                PASSWORD_LOCATORS
        );

        healingDriver.clickWithHealing(
                "Login button",
                LOGIN_BTN_LOCATORS
        );

        intelligentWait.waitForStableElement(
                By.cssSelector("[data-testid='add-new-note']"),
                15
        );

        boolean dashboardVisible =
                healingDriver.isVisibleWithHealing(
                        "Add Note button",
                        ADD_NOTE_LOCATORS
                );

        Assert.assertTrue(
                dashboardVisible,
                "Dashboard should load after self-healing login."
        );

        log.info("TC-AGENT-01 PASSED");
    }

    // TC-AGENT-02
    @Test(description = "TC-AGENT-02: Self-healing note creation")
    @Story("Agentic - Self-healing note creation")
    @Severity(SeverityLevel.CRITICAL)
    public void testSelfHealingNoteCreation() {

        log.info("Running TC-AGENT-02");

        // Login
        healingDriver.typeWithHealing(
                "Email field",
                ConfigReader.getEmail(),
                EMAIL_LOCATORS
        );

        healingDriver.typeWithHealing(
                "Password field",
                ConfigReader.getPassword(),
                PASSWORD_LOCATORS
        );

        healingDriver.clickWithHealing(
                "Login button",
                LOGIN_BTN_LOCATORS
        );

        intelligentWait.waitForStableElement(
                By.cssSelector("[data-testid='add-new-note']"),
                15
        );

        // Add note
        healingDriver.clickWithHealing(
                "Add Note button",
                ADD_NOTE_LOCATORS
        );

        String noteTitle =
                "AgenticNote_" + System.currentTimeMillis();

        intelligentWait.waitForStableElement(
                By.id("title"),
                10
        );

        healingDriver.typeWithHealing(
                "Title field",
                noteTitle,
                TITLE_LOCATORS
        );

        healingDriver.typeWithHealing(
                "Description field",
                "Created by self-healing automation",
                DESC_LOCATORS
        );

        // Category dropdown
        org.openqa.selenium.WebElement categoryDropdown =
                healingDriver.findWithHealing(
                        "Category dropdown",
                        By.id("category"),
                        By.name("category"),
                        By.cssSelector("select#category")
                );

        new org.openqa.selenium.support.ui.Select(categoryDropdown)
                .selectByVisibleText("Home");

        // Save + rerun logic
        healingDriver.clickWithHealing(
                "Save button",
                SAVE_LOCATORS
        );

        intelligentWait.waitForStableElement(
                By.cssSelector("[data-testid='add-new-note']"),
                15
        );

        intelligentWait.waitForTextAppear(
                noteTitle,
                15
        );

        boolean noteVisible =
                healingDriver.isVisibleWithHealing(
                        "Created note title",
                        By.xpath("//*[contains(text(),'" + noteTitle + "')]")
                );

        Assert.assertTrue(
                noteVisible,
                "Created note should appear in dashboard."
        );

        log.info("TC-AGENT-02 PASSED");
    }

    // TC-AGENT-03
    @Test(description = "TC-AGENT-03: Decision-based rerun logic")
    @Story("Agentic - Decision rerun")
    @Severity(SeverityLevel.NORMAL)
    public void testDecisionBasedRerun() {

        log.info("Running TC-AGENT-03");

        healingDriver.typeWithHealing(
                "Email field",
                ConfigReader.getEmail(),
                EMAIL_LOCATORS
        );

        healingDriver.typeWithHealing(
                "Password field",
                ConfigReader.getPassword(),
                PASSWORD_LOCATORS
        );

        healingDriver.clickWithHealing(
                "Login button",
                LOGIN_BTN_LOCATORS
        );

        intelligentWait.waitForStableElement(
                By.cssSelector("[data-testid='add-new-note']"),
                15
        );

        Assert.assertTrue(
                healingDriver.isVisibleWithHealing(
                        "Dashboard Add Note button",
                        ADD_NOTE_LOCATORS
                ),
                "Dashboard should load successfully after rerun."
        );

        log.info("TC-AGENT-03 PASSED");
    }
}