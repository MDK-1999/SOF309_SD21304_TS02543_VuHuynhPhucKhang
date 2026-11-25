package com.poly.sof309.ui.tests;

import com.poly.sof309.ui.pages.LoginPage;
import com.poly.sof309.ui.pages.RegisterPage;
import com.poly.sof309.ui.pages.RegisterPage.Registration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegisterTest extends UiTestSupport {
    @Test
    @DisplayName("UI-REGISTER-01: Đăng ký thành công và đăng nhập bằng tài khoản mới")
    void customerCanRegisterAndLogin() {
        Registration account = sampleAccount();
        RegisterPage register = new RegisterPage(driver, baseUrl);
        register.open();
        register.register(account, account.password());

        LoginPage login = new LoginPage(driver, baseUrl);
        assertEquals("Đăng ký thành công", login.messageTitle());
        assertEquals(baseUrl + "/login?success=registered", login.waitForUrl("/login?success=registered"));

        login.login(account.username(), account.password());
        assertEquals(baseUrl + "/customer/orders", login.waitForUrl("/customer/orders"));
    }

    @Test
    @DisplayName("UI-REGISTER-02: Xác nhận mật khẩu khác bị từ chối")
    void mismatchedPasswordShowsError() {
        Registration account = sampleAccount();
        RegisterPage register = new RegisterPage(driver, baseUrl);
        register.open();
        register.register(account, "Different123");

        assertEquals("Mật khẩu xác nhận không khớp.", register.errorMessage());
        assertEquals(baseUrl + "/register", register.waitForUrl("/register"));
    }

    private Registration sampleAccount() {
        String username = "gearqa" + runId();
        return new Registration("Khách kiểm thử", username, "GearUiTest123",
                username + "@example.test", "0901234567", "123 Đường Kiểm Thử");
    }
}