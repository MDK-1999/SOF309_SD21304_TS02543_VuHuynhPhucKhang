package com.poly.sof309.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

public final class ProductPage extends BasePage {
    private static final By TABLE = By.id("ptbl");
    private static final By ADD = By.id("addProductButton");
    private static final By ROWS = By.cssSelector("#ptbl tbody tr[data-id]");
    private static final By EDIT = By.cssSelector("button.edit-product-btn");
    private static final By FORM = By.cssSelector("#addModal form");
    private static final By NAME = By.id("fName");
    private static final By CATEGORY = By.id("fCat");
    private static final By PRICE = By.id("fPrice");
    private static final By STOCK = By.id("fStock");
    private static final By ACTIVE = By.id("fActive");
    private static final By SAVE = By.cssSelector("#addModal button[type='submit']");
    private static final By CANCEL = By.cssSelector("#addModal .modal-footer .btn-ghost");
    private static final By SUCCESS = By.cssSelector(".content .alert-success");

    public ProductPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }

    public void open() {
        navigate("/staff/products");
        visible(TABLE);
    }

    public void add(String name, String categoryId, int price, int stock) {
        click(ADD);
        visible(FORM);
        fill(NAME, name);
        new Select(visible(CATEGORY)).selectByValue(categoryId);
        fill(PRICE, String.valueOf(price));
        fill(STOCK, String.valueOf(stock));
        save();
    }

    public void edit(String id, String name, int price) {
        openEdit(id);
        fill(NAME, name);
        fill(PRICE, String.valueOf(price));
        save();
    }

    public void cancelEdit(String id, String unsavedName) {
        openEdit(id);
        fill(NAME, unsavedName);
        click(CANCEL);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(FORM));
    }

    public void stopSelling(String id) {
        openEdit(id);
        new Select(visible(ACTIVE)).selectByValue("false");
        save();
    }

    public String successMessage() {
        return visible(SUCCESS).getText();
    }

    public ProductRow findRow(String name) {
        return wait.until(browser -> {
            for (WebElement row : browser.findElements(ROWS)) {
                try {
                    if (name.equals(row.getDomAttribute("data-name"))) {
                        return new ProductRow(row.getDomAttribute("data-id"), row.getDomAttribute("data-name"),
                                row.getDomAttribute("data-price"), Integer.parseInt(row.getDomAttribute("data-stock")),
                                Boolean.parseBoolean(row.getDomAttribute("data-active")));
                    }
                } catch (StaleElementReferenceException ignored) {
                    return null;
                }
            }
            return null;
        });
    }

    private void openEdit(String id) {
        for (WebElement row : driver.findElements(ROWS)) {
            if (id.equals(row.getDomAttribute("data-id"))) {
                wait.until(ExpectedConditions.elementToBeClickable(row.findElement(EDIT))).click();
                visible(FORM);
                return;
            }
        }
        throw new IllegalStateException("Không tìm thấy sản phẩm có ID " + id);
    }

    private void save() {
        WebElement form = visible(FORM);
        click(SAVE);
        wait.until(ExpectedConditions.stalenessOf(form));
    }

    public record ProductRow(String id, String name, String price, int stock, boolean active) {}
}