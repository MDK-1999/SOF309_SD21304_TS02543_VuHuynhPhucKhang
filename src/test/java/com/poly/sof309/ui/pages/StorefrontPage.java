package com.poly.sof309.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public final class StorefrontPage extends BasePage {
    private static final By PRODUCT_TITLE = By.cssSelector(".product-detail h1");
    private static final By NOT_FOUND_CODE = By.cssSelector(".err-code");

    public StorefrontPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }

    public void openProduct(String id) {
        navigate("/product?id=" + id);
    }

    public String productTitle() {
        return visible(PRODUCT_TITLE).getText();
    }

    public String notFoundCode() {
        return visible(NOT_FOUND_CODE).getText();
    }
}