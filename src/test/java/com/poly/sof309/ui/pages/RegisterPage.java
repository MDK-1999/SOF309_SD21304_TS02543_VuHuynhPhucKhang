package com.poly.sof309.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public final class RegisterPage extends BasePage {
    private static final By FULL_NAME = By.id("fullName");
    private static final By USERNAME = By.id("username");
    private static final By PASSWORD = By.id("password");
    private static final By CONFIRM = By.id("confirm");
    private static final By EMAIL = By.id("email");
    private static final By PHONE = By.id("phone");
    private static final By ADDRESS = By.id("address");
    private static final By SUBMIT = By.cssSelector("form[action$='/register'] button[type='submit']");
    private static final By ERROR = By.cssSelector(".alert[role='alert']");

    public RegisterPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }

    public void open() {
        navigate("/register");
        visible(FULL_NAME);
    }

    public void register(Registration account, String confirmation) {
        fill(FULL_NAME, account.fullName());
        fill(USERNAME, account.username());
        fill(PASSWORD, account.password());
        fill(CONFIRM, confirmation);
        fill(EMAIL, account.email());
        fill(PHONE, account.phone());
        fill(ADDRESS, account.address());
        click(SUBMIT);
    }

    public String errorMessage() {
        return visible(ERROR).getText();
    }

    public record Registration(String fullName, String username, String password,
                               String email, String phone, String address) {}
}