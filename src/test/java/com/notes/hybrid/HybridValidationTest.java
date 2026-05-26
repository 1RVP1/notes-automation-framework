package com.notes.hybrid;

import com.notes.config.ConfigReader;
import com.notes.pages.CreateNotePage;
import com.notes.pages.DashboardPage;
import com.notes.pages.LoginPage;
import com.notes.utils.ApiHelper;
import com.notes.utils.BaseTest;
import com.notes.utils.DriverManager;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

@Epic("Notes Application")
@Feature("Hybrid - UI + API Sync")
public class HybridValidationTest extends BaseTest {

    private DashboardPage dashboardPage;
    private static final String HYBRID_TITLE = "HybridTest_Note_" + System.currentTimeMillis();

    @BeforeMethod(alwaysRun = true)
    public void setupHybrid() {
        dashboardPage = new LoginPage().loginAs(
                ConfigReader.getEmail(),
                ConfigReader.getPassword()
        );
        ApiHelper.loginAndGetToken(
                ConfigReader.getEmail(),
                ConfigReader.getPassword()
        );
    }

    // TC-10 | TS-10
    @Test(description = "TC-10: Validate UI-created note appears in API response")
    @Story("TS-10 - UI note visible in API")
    @Severity(SeverityLevel.CRITICAL)
    public void testUiNoteAppearsInApi() {
        log.info("Running TC-10: UI Note in API");

        // Step 1: Create note via UI, wait for dashboard to re-render
        dashboardPage.clickAddNote();
        new CreateNotePage().createNote(HYBRID_TITLE, "Hybrid test description", "Home");
        new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(20))
                .until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("[data-testid='add-new-note']")));
        dashboardPage = new DashboardPage();
        Assert.assertTrue(dashboardPage.isNotePresent(HYBRID_TITLE),
                "Note should appear in UI after creation.");

        // Step 2: Verify via API
        Response response = ApiHelper.getAllNotes();
        Assert.assertEquals(response.getStatusCode(), 200);
        List<String> apiTitles = response.jsonPath().getList("data.title");
        Assert.assertTrue(apiTitles.contains(HYBRID_TITLE),
                "UI-created note should appear in GET /notes API response. Title: " + HYBRID_TITLE);
        log.info("TC-10 PASSED: Note '" + HYBRID_TITLE + "' found in API response.");
    }

    // TC-11 | TS-11
    @Test(description = "TC-11: Validate deleted note (via API) disappears from UI")
    @Story("TS-11 - API deleted note removed from UI")
    @Severity(SeverityLevel.CRITICAL)
    public void testApiDeletedNoteRemovedFromUi() {
        log.info("Running TC-11: API Delete reflected in UI");

        // Step 1: Create note via UI, wait for dashboard to re-render
        String deleteTitle = "DeleteTest_" + System.currentTimeMillis();
        dashboardPage.clickAddNote();
        new CreateNotePage().createNote(deleteTitle, "To be deleted via API", "Work");
        new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(20))
                .until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("[data-testid='add-new-note']")));
        dashboardPage = new DashboardPage();
        Assert.assertTrue(dashboardPage.isNotePresent(deleteTitle),
                "Note should exist in UI before deletion.");

        // Step 2: Get note ID from API
        Response allNotes = ApiHelper.getAllNotes();
        List<String> titles = allNotes.jsonPath().getList("data.title");
        List<String> ids    = allNotes.jsonPath().getList("data.id");
        int index = titles.indexOf(deleteTitle);
        Assert.assertTrue(index >= 0, "Note should exist in API before deletion.");
        String noteId = ids.get(index);
        log.info("Note ID to delete: " + noteId);

        // Step 3: Delete via API
        Response deleteResponse = ApiHelper.deleteNoteApi(noteId);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200,
                "DELETE /notes/{id} should return 200.");

        // Step 4: Refresh dashboard and verify note removal
        log.info("Navigating to dashboard to verify note removal.");
        DriverManager.getDriver().get(ConfigReader.getBaseUrl());
        new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(30))
                .until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("[data-testid='add-new-note']")));
        dashboardPage = new DashboardPage();
        Assert.assertFalse(dashboardPage.isNotePresent(deleteTitle),
                "Note deleted via API should not appear in UI after page reload.");
    }
}