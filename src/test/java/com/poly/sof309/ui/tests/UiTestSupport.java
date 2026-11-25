package com.poly.sof309.ui.tests;

import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

@Tag("ui")
abstract class UiTestSupport {
    protected final String baseUrl = System.getProperty("gearastral.ui.baseUrl", "http://127.0.0.1:8080")
            .replaceFirst("/+$", "");
    protected final String staffUser = System.getProperty("gearastral.ui.staffUser", "demostaff");
    protected final String staffPassword = System.getProperty("gearastral.ui.staffPassword", "GearDemo123");
    protected WebDriver driver;

    @BeforeEach
    void openChrome() {
        ChromeOptions options = new ChromeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        options.addArguments("--window-size=1440,1000");
        if (Boolean.getBoolean("gearastral.ui.headless")) {
            options.addArguments("--headless=new");
        }
        String chromeBinary = System.getProperty("gearastral.ui.chromeBinary");
        if (chromeBinary != null && !chromeBinary.isBlank()) {
            options.setBinary(chromeBinary);
        }
        driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
    }

    @AfterEach
    void closeChrome() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected String runId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
