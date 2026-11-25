package com.poly.sof309.ui.tests;

import com.poly.sof309.ui.pages.LoginPage;
import com.poly.sof309.ui.pages.ProductPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnabledIfSystemProperty(named = "gearastral.ui.enabled", matches = "true")
class LoginTest extends UiTestSupport {
    @Test
    @DisplayName("UI-LOGIN-01: Nhân viên đăng nhập và mở trang sản phẩm")
    void staffCanLoginAndManageProducts() {
        LoginPage login = new LoginPage(driver, baseUrl);
        login.open();
        login.login(staffUser, staffPassword);

        assertEquals(baseUrl + "/dashboard", login.waitForUrl("/dashboard"));
        ProductPage products = new ProductPage(driver, baseUrl);
        products.open();
        assertEquals(baseUrl + "/staff/products", products.waitForUrl("/staff/products"));
    }

    @Test
    @DisplayName("UI-LOGIN-02: Mật khẩu sai hiển thị thông báo")
    void wrongPasswordShowsError() {
        LoginPage login = new LoginPage(driver, baseUrl);
        login.open();
        login.login(staffUser, "Wrong" + runId());

        assertEquals("ĐĂNG NHẬP THẤT BẠI", login.messageTitle());
        assertEquals(baseUrl + "/login", login.waitForUrl("/login"));
    }
}
