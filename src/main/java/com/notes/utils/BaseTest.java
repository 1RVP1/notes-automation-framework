package com.notes.utils;

import com.notes.config.ConfigReader;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;

public class BaseTest {

    protected static final Logger log = LogManager.getLogger(BaseTest.class);

    @BeforeMethod
    public void setUp() {
        DriverManager.initDriver();
        DriverManager.getDriver().get(ConfigReader.getBaseUrl() + "/login");
        log.info("Browser launched and navigated to: " + ConfigReader.getBaseUrl());
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            log.error("Test FAILED: " + result.getName());
            takeScreenshot(result.getName());
        } else {
            log.info("Test PASSED: " + result.getName());
        }
        DriverManager.quitDriver();
    }

    private void takeScreenshot(String testName) {
        try {
            byte[] screenshot = ((TakesScreenshot) DriverManager.getDriver())
                    .getScreenshotAs(OutputType.BYTES);

            // Save to target/screenshots folder
            File dir = new File("target/screenshots");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, testName + "_" + System.currentTimeMillis() + ".png");
            Files.write(file.toPath(), screenshot);
            log.info("Screenshot saved to disk: " + file.getAbsolutePath());

            // ✅ Correct way to attach to Allure — ByteArrayInputStream is what Allure expects
            Allure.addAttachment(
                    "Screenshot on Failure - " + testName,
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    "png"
            );
            log.info("Screenshot attached to Allure report for: " + testName);

        } catch (Exception e) {
            log.warn("Could not capture screenshot: " + e.getMessage());
        }
    }

    @AfterSuite
    public void cleanupAllNotes() {
        log.info("=== CLEANUP: Deleting all remaining notes via API ===");
        try {
            String token = ApiHelper.loginAndGetToken(
                    ConfigReader.getEmail(),
                    ConfigReader.getPassword()
            );

            if (token == null) {
                log.warn("Cleanup skipped — could not get auth token.");
                return;
            }

            io.restassured.response.Response response = ApiHelper.getAllNotes();
            java.util.List<String> ids = response.jsonPath().getList("data.id");

            if (ids == null || ids.isEmpty()) {
                log.info("No notes to delete.");
                return;
            }

            for (String id : ids) {
                ApiHelper.deleteNoteApi(id);
                log.info("Deleted note ID: " + id);
            }

            log.info("=== CLEANUP DONE: " + ids.size() + " notes deleted ===");

        } catch (Exception e) {
            log.warn("Cleanup failed: " + e.getMessage());
        }
    }
}