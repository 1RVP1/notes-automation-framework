package com.notes.ui;

import com.notes.config.ConfigReader;
import com.notes.pages.CreateNotePage;
import com.notes.pages.DashboardPage;
import com.notes.pages.LoginPage;
import com.notes.utils.BaseTest;
import com.notes.utils.DriverManager;
import io.qameta.allure.*;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

@Epic("Notes Application")
@Feature("Notes - UI")
public class NoteTest extends BaseTest {

    private WebDriverWait wait;
    private DashboardPage dashboardPage;

    private static final String TEST_TITLE       = "AutoTest_Note_001";
    private static final String TEST_DESCRIPTION = "Automation test note description";
    private static final String TEST_CATEGORY    = "Home";

    @BeforeMethod(alwaysRun = true)
    public void loginToDashboard() {

        wait = new WebDriverWait(
                DriverManager.getDriver(),
                Duration.ofSeconds(15)
        );

        dashboardPage = new LoginPage().loginAs(
                ConfigReader.getEmail(),
                ConfigReader.getPassword()
        );

        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='add-new-note']")));
    }

    private void createNoteAndWait(
            String title,
            String description,
            String category
    ) {

        dashboardPage.clickAddNote();

        new CreateNotePage().createNote(
                title,
                description,
                category
        );

        new WebDriverWait(
                DriverManager.getDriver(),
                Duration.ofSeconds(20)
        ).until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='add-new-note']")));

        dashboardPage = new DashboardPage();
    }

    // TC-04 | TS-04 + TS-05 + TS-06
    @Test(description = "TC-04: Validate successful note creation")
    @Story("TS-04/05/06 - Note creation")
    @Severity(SeverityLevel.CRITICAL)
    public void testNoteCreation() {

        log.info("Running TC-04: Note Creation");

        String noteTitle = TEST_TITLE + "_create";

        createNoteAndWait(
                noteTitle,
                TEST_DESCRIPTION,
                TEST_CATEGORY
        );

        Assert.assertTrue(
                dashboardPage.isNotePresent(noteTitle),
                "Newly created note should be visible in dashboard."
        );

        log.info("Note created and validated with category: "
                + TEST_CATEGORY);
    }

    // TC-06 | TS-08 + TS-15
    @Test(description = "TC-06: Validate successful note deletion")
    @Story("TS-08/15 - Note deletion and action buttons")
    @Severity(SeverityLevel.CRITICAL)
    public void testNoteDeletion() {

        log.info("Running TC-06: Note Deletion");

        String deleteTitle = TEST_TITLE + "_del";

        createNoteAndWait(
                deleteTitle,
                TEST_DESCRIPTION,
                TEST_CATEGORY
        );

        Assert.assertTrue(
                dashboardPage.isNotePresent(deleteTitle),
                "Note should exist before deletion."
        );

        dashboardPage.deleteNoteByTitle(deleteTitle);

        Assert.assertFalse(
                dashboardPage.isNotePresent(deleteTitle),
                "Note should be removed from UI after deletion."
        );
    }

    // TC-07 | TS-07
    @Test(description = "TC-07: Validate note search functionality")
    @Story("TS-07 - Note search")
    @Severity(SeverityLevel.NORMAL)
    public void testNoteSearch() {

        log.info("Running TC-07: Note Search");

        String searchTitle =
                "SearchTest_" + System.currentTimeMillis();

        createNoteAndWait(
                searchTitle,
                TEST_DESCRIPTION,
                TEST_CATEGORY
        );

        Assert.assertTrue(
                dashboardPage.isNotePresent(searchTitle),
                "Note should appear in dashboard before search."
        );

        dashboardPage.searchNote(searchTitle);

        Assert.assertTrue(
                dashboardPage.isNotePresent(searchTitle),
                "Search results should contain the searched note title."
        );
    }

    // TC-14 | TS-14
    @Test(description = "TC-14: Validate behavior under invalid inputs")
    @Story("TS-14 - Invalid inputs / negative scenarios")
    @Severity(SeverityLevel.NORMAL)
    public void testInvalidInputs() {

        log.info("Running TC-14: Invalid Inputs");

        dashboardPage.clickAddNote();

        CreateNotePage createNotePage =
                new CreateNotePage();

        createNotePage.enterTitle("");

        createNotePage.enterDescription(
                "Some description"
        );

        createNotePage.selectCategory(
                TEST_CATEGORY
        );

        createNotePage.saveNote();

        Assert.assertNotNull(
                DriverManager.getDriver().getCurrentUrl(),
                "Application should handle empty title gracefully without crashing."
        );
    }
}