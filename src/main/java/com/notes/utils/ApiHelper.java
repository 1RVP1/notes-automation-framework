package com.notes.utils;

import com.notes.config.ConfigReader;
import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.restassured.RestAssured.given;

public class ApiHelper {

    private static final Logger log = LogManager.getLogger(ApiHelper.class);

    // Thread-safe auth token for parallel execution
    private static final ThreadLocal<String> authToken = new ThreadLocal<>();

    static {
        RestAssured.baseURI = ConfigReader.getApiBaseUrl();
    }

    private static RequestSpecification baseRequest() {
        return given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
    }

    private static RequestSpecification authRequest() {
        return baseRequest()
                .header("x-auth-token", authToken.get());
    }

    // ── Authentication ────────────────────────────────────────────────

    public static Response loginApi(String email, String password) {

        String body = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\"}",
                email,
                password
        );

        Response response = baseRequest()
                .body(body)
                .post("/users/login");

        log.info("POST /users/login → Status: " + response.getStatusCode());

        Allure.addAttachment(
                "Login API Response",
                response.asPrettyString()
        );

        return response;
    }

    public static void setAuthToken(String token) {
        authToken.set(token);
        log.info("Auth token set.");
    }

    public static String getAuthToken() {
        return authToken.get();
    }

    public static String loginAndGetToken(String email, String password) {

        Response response = loginApi(email, password);

        String token = response.jsonPath().getString("data.token");

        setAuthToken(token);

        return token;
    }

    // ── Notes API ─────────────────────────────────────────────────────

    public static Response getAllNotes() {

        Response response = authRequest().get("/notes");

        log.info(
                "GET /notes → Status: "
                        + response.getStatusCode()
                        + ", Time: "
                        + response.getTime()
                        + "ms"
        );

        Allure.addAttachment(
                "GET /notes Response",
                response.asPrettyString()
        );

        return response;
    }

    /**
     * Retries GET /notes up to maxRetries times.
     * Returns the first successful 200 response.
     * Throws RuntimeException if all retries fail.
     */
    public static Response getAllNotesWithRetry(int maxRetries) {

        int attempt = 0;

        while (attempt < maxRetries) {

            attempt++;

            log.info(
                    "GET /notes attempt "
                            + attempt
                            + " of "
                            + maxRetries
            );

            try {

                Response response = authRequest().get("/notes");

                log.info(
                        "Attempt "
                                + attempt
                                + " → Status: "
                                + response.getStatusCode()
                                + ", Time: "
                                + response.getTime()
                                + "ms"
                );

                if (response.getStatusCode() == 200) {

                    Allure.addAttachment(
                            "GET /notes Retry Response (attempt " + attempt + ")",
                            response.asPrettyString()
                    );

                    return response;
                }

                log.warn(
                        "Attempt "
                                + attempt
                                + " failed with status: "
                                + response.getStatusCode()
                );

            } catch (Exception e) {

                log.warn(
                        "Attempt "
                                + attempt
                                + " threw exception: "
                                + e.getMessage()
                );
            }

            if (attempt < maxRetries) {

                try {
                    Thread.sleep(1000L * attempt);
                } catch (InterruptedException ignored) {
                }
            }
        }

        throw new RuntimeException(
                "GET /notes failed after "
                        + maxRetries
                        + " retries."
        );
    }

    public static Response createNoteApi(
            String title,
            String description,
            String category
    ) {

        String body = String.format(
                "{\"title\":\"%s\",\"description\":\"%s\",\"category\":\"%s\"}",
                title,
                description,
                category
        );

        Response response = authRequest()
                .body(body)
                .post("/notes");

        log.info(
                "POST /notes → Status: "
                        + response.getStatusCode()
        );

        Allure.addAttachment(
                "Create Note API Response",
                response.asPrettyString()
        );

        return response;
    }

    public static Response deleteNoteApi(String noteId) {

        Response response = authRequest()
                .delete("/notes/" + noteId);

        log.info(
                "DELETE /notes/"
                        + noteId
                        + " → Status: "
                        + response.getStatusCode()
        );

        Allure.addAttachment(
                "Delete Note API Response",
                response.asPrettyString()
        );

        return response;
    }

    public static long getResponseTime(Response response) {
        return response.getTime();
    }
}