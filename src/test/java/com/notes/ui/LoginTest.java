package com.notes.ui;

import com.notes.config.ConfigReader;
import com.notes.pages.DashboardPage;
import com.notes.pages.LoginPage;
import com.notes.utils.BaseTest;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.Test;

@Epic("Notes Application")
@Feature("Login")
public class LoginTest extends BaseTest {

    // TC-01 | TS-01
    @Test(description = "TC-01: Validate login with valid credentials")
    @Story("TS-01 - Successful login")
    @Severity(SeverityLevel.BLOCKER)
    public void testValidLogin() {
        log.info("Running TC-01: Valid Login");
        LoginPage loginPage = new LoginPage();
        DashboardPage dashboard = loginPage.loginAs(
                ConfigReader.getEmail(),
                ConfigReader.getPassword()
        );
        Assert.assertTrue(dashboard.isDashboardLoaded(),
                "Dashboard should load after successful login.");
    }

    // TC-02 | TS-02
    @Test(description = "TC-02: Validate login with invalid credentials")
    @Story("TS-02 - Invalid login")
    @Severity(SeverityLevel.CRITICAL)
    public void testInvalidLogin() {
        log.info("Running TC-02: Invalid Login");
        LoginPage loginPage = new LoginPage();
        loginPage.loginAs("wrong@email.com", "WrongPass@999");
        Assert.assertTrue(loginPage.isErrorDisplayed(),
                "Error message should be displayed for invalid credentials.");
    }

    // TC-03 | TS-03
    @Test(description = "TC-03: Validate mandatory field validations on login")
    @Story("TS-03 - Mandatory field validation")
    @Severity(SeverityLevel.NORMAL)
    public void testMandatoryFieldValidation() {
        log.info("Running TC-03: Mandatory Field Validation");
        LoginPage loginPage = new LoginPage();
        loginPage.clickLogin();
        // HTML5 validation — URL should still be login page
        String url = com.notes.utils.DriverManager.getDriver().getCurrentUrl();
        Assert.assertTrue(url.contains("login"),
                "User should remain on login page when fields are empty.");
    }
}
