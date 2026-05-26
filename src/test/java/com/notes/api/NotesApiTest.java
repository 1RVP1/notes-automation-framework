package com.notes.api;

import com.notes.config.ConfigReader;
import com.notes.utils.ApiHelper;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static io.restassured.RestAssured.given;

@Epic("Notes Application")
@Feature("Notes - API")
public class NotesApiTest {

    private static final Logger log =
            LogManager.getLogger(NotesApiTest.class);

    @BeforeClass
    public void apiSetup() {

        log.info("Setting up API tests.");

        String token = ApiHelper.loginAndGetToken(
                ConfigReader.getEmail(),
                ConfigReader.getPassword()
        );

        Assert.assertNotNull(
                token,
                "Auth token must not be null."
        );

        log.info("Auth token obtained successfully.");
    }

    // TC-09 | TS-09
    @Test(description = "TC-09: Validate GET /notes API returns notes list")
    @Story("TS-09 - GET /notes API")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetNotesApi() {

        log.info("Running TC-09: GET /notes API");

        Response response =
                ApiHelper.getAllNotes();

        Assert.assertEquals(
                response.getStatusCode(),
                200,
                "GET /notes should return HTTP 200."
        );

        Assert.assertNotNull(
                response.jsonPath().get("data"),
                "Response body should contain data field."
        );

        Assert.assertTrue(
                response.jsonPath().getBoolean("success"),
                "Response success field should be true."
        );

        List<String> titles =
                response.jsonPath().getList("data.title");

        Assert.assertNotNull(
                titles,
                "Notes list should not be null."
        );
    }

    // TC-12 | TS-12
    @Test(description = "TC-12: Validate API response time")
    @Story("TS-12 - API Response Time")
    @Severity(SeverityLevel.NORMAL)
    public void testApiResponseTime() {

        log.info("Running TC-12: API Response Time");

        Response response =
                ApiHelper.getAllNotes();

        long responseTime =
                ApiHelper.getResponseTime(response);

        log.info("Response time: "
                + responseTime + "ms");

        Assert.assertTrue(
                responseTime < ConfigReader.getApiResponseLimit(),
                "API response time should be less than "
                        + ConfigReader.getApiResponseLimit()
                        + "ms"
        );
    }

    // TC-NEG-01
    @Test(description = "TC-NEG-01: Validate GET /notes without auth token")
    @Story("Negative API - Missing auth token")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetNotesWithoutToken() {

        log.info("Running TC-NEG-01");

        Response response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .get(ConfigReader.getApiBaseUrl() + "/notes");

        log.info("Status without token: "
                + response.getStatusCode());

        Assert.assertEquals(
                response.getStatusCode(),
                401,
                "GET /notes without token should return 401."
        );
    }

    // TC-NEG-02
    @Test(description = "TC-NEG-02: Validate POST /notes with missing title")
    @Story("Negative API - Missing title")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreateNoteWithMissingTitle() {

        log.info("Running TC-NEG-02");

        Response response =
                ApiHelper.createNoteApi(
                        "",
                        "Some description",
                        "Home"
                );

        log.info("Status for missing title: "
                + response.getStatusCode());

        Assert.assertTrue(
                response.getStatusCode() == 400
                        || response.getStatusCode() == 422,
                "POST /notes with empty title should fail."
        );
    }

    // TC-NEG-03
    @Test(description = "TC-NEG-03: Validate login with wrong password")
    @Story("Negative API - Invalid login")
    @Severity(SeverityLevel.CRITICAL)
    public void testLoginWithWrongPassword() {

        log.info("Running TC-NEG-03");

        Response response =
                ApiHelper.loginApi(
                        ConfigReader.getEmail(),
                        "WrongPassword@999"
                );

        log.info("Status for wrong password: "
                + response.getStatusCode());

        Assert.assertEquals(
                response.getStatusCode(),
                401,
                "Login with wrong password should return 401."
        );

        Assert.assertFalse(
                response.jsonPath().getBoolean("success"),
                "Login success field should be false."
        );
    }

    // TC-SCHEMA-01
    @Test(description = "TC-SCHEMA-01: Validate GET /notes response schema")
    @Story("Schema Validation")
    @Severity(SeverityLevel.NORMAL)
    public void testGetNotesJsonSchema() {

        log.info("Running TC-SCHEMA-01");

        Response response =
                ApiHelper.getAllNotes();

        Assert.assertEquals(
                response.getStatusCode(),
                200
        );

        Assert.assertNotNull(
                response.jsonPath().get("success"),
                "Response must contain success field."
        );

        Assert.assertNotNull(
                response.jsonPath().get("status"),
                "Response must contain status field."
        );

        Assert.assertNotNull(
                response.jsonPath().get("message"),
                "Response must contain message field."
        );

        Assert.assertNotNull(
                response.jsonPath().get("data"),
                "Response must contain data field."
        );

        Assert.assertTrue(
                response.jsonPath().getBoolean("success"),
                "Success should be true."
        );

        Assert.assertEquals(
                response.jsonPath().getInt("status"),
                200,
                "Status field should be 200."
        );

        log.info("Schema validation successful.");
    }

    // TC-RETRY-01
    @Test(description = "TC-RETRY-01: Validate API retry mechanism")
    @Story("Retry Mechanism")
    @Severity(SeverityLevel.NORMAL)
    public void testApiRetryMechanism() {

        log.info("Running TC-RETRY-01");

        Response response =
                ApiHelper.getAllNotesWithRetry(3);

        Assert.assertEquals(
                response.getStatusCode(),
                200,
                "GET /notes with retry should return 200."
        );

        log.info("Retry mechanism validated successfully.");
    }
}