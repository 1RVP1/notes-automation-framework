package com.notes.performance;

import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class JMeterRunner {

    private static final Logger log =
            LogManager.getLogger(JMeterRunner.class);

    public static void runPerformanceTest() {

        try {

            String command =
                    "jmeter -n " +
                            "-t performance/NotesApiPerformance.jmx " +
                            "-l performance/results/results.jtl " +
                            "-e -o performance/results/dashboard";

            log.info("Starting JMeter Performance Test...");

            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe",
                    "/c",
                    command
            );

            builder.redirectErrorStream(true);

            Process process = builder.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;

            while ((line = reader.readLine()) != null) {

                log.info("[JMeter] " + line);
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {

                log.info("JMeter performance test completed successfully.");

                // Attach JTL Results
                File resultsFile =
                        new File("performance/results/results.jtl");

                if (resultsFile.exists()) {

                    Allure.addAttachment(
                            "JMeter Results",
                            new FileInputStream(resultsFile)
                    );
                }

                // Attach HTML Dashboard
                File dashboard =
                        new File("performance/results/dashboard/index.html");

                if (dashboard.exists()) {

                    Allure.addAttachment(
                            "JMeter Dashboard",
                            new FileInputStream(dashboard)
                    );
                }

            } else {

                log.error(
                        "JMeter execution failed with exit code: "
                                + exitCode
                );
            }

        } catch (Exception e) {

            log.error(
                    "Failed to execute JMeter test: "
                            + e.getMessage()
            );
        }
    }
}