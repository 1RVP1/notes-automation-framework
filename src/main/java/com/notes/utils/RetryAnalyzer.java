package com.notes.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Agentic auto-retry mechanism for flaky UI/API tests.
 *
 * Attach to any @Test with: @Test(retryAnalyzer = RetryAnalyzer.class)
 * Or set globally via RetryListener on the TestNG suite.
 *
 * Retries up to MAX_RETRY times on failure before marking the test as failed.
 * Logs each retry attempt for traceability in the Allure report.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LogManager.getLogger(RetryAnalyzer.class);
    private static final int MAX_RETRY = 2;
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY) {
            retryCount++;
            log.warn("[Retry] Test '{}' FAILED — attempt {}/{}. Retrying...",
                    result.getName(), retryCount, MAX_RETRY);
            return true;
        }
        log.error("[Retry] Test '{}' failed after {} retries. Marking as FAILED.",
                result.getName(), MAX_RETRY);
        return false;
    }
}