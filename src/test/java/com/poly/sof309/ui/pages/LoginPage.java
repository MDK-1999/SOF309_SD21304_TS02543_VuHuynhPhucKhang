package com.poly.sof309.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public final class LoginPage extends BasePage {
    private static final By USERNAME = By.id("loginUsername");
    private static final By PASSWORD = By.id("loginPassword");
    private static final By SUBMIT = By.cssSelector("form[action$='/login'] button[type='submit']");
    private static final By MESSAGE_TITLE = By.cssSelector("#toast .toast-title");

    public LoginPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }

    public void open() {
        navigate("/login");
        visible(USERNAME);
    }

    public void login(String username, String password) {
        fill(USERNAME, username);
        fill(PASSWORD, password);
        click(SUBMIT);
    }

    public String messageTitle() {
        return visible(MESSAGE_TITLE).getText();
    }
}