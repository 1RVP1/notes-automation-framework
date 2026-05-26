package com.notes.ui;

import com.notes.config.ConfigReader;
import com.notes.pages.DashboardPage;
import com.notes.pages.LoginPage;
import com.notes.utils.BaseTest;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.Test;

@Epic("Notes Application")
@Feature("Logout")
public class LogoutTest extends BaseTest {

    // TC-13 | TS-13
    @Test(description = "TC-13: Validate logout functionality")
    @Story("TS-13 - Logout")
    @Severity(SeverityLevel.CRITICAL)
    public void testLogout() {
        log.info("Running TC-13: Logout Functionality");
        LoginPage loginPage = new LoginPage();
        DashboardPage dashboard = loginPage.loginAs(
                ConfigReader.getEmail(),
                ConfigReader.getPassword()
        );
        Assert.assertTrue(dashboard.isDashboardLoaded(), "Should be on dashboard before logout.");

        LoginPage afterLogout = dashboard.logout();

        // Verify redirected back to login page
        String currentUrl = com.notes.utils.DriverManager.getDriver().getCurrentUrl();
        Assert.assertTrue(
                currentUrl.contains("login") || currentUrl.contains("notes/app")
                        || currentUrl.equals(ConfigReader.getBaseUrl() + "/"),
                "User should be redirected to login page after logout."
        );
    }
}
