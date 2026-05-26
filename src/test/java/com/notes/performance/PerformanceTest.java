package com.notes.performance;

import io.qameta.allure.*;
import org.testng.annotations.Test;

@Epic("Performance Testing")
@Feature("JMeter Performance Validation")

public class PerformanceTest {

    @Test(description = "Validate Notes API performance using JMeter")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Performance Testing with JMeter")

    public void runJMeterPerformanceSuite() {

        JMeterRunner.runPerformanceTest();
    }
}