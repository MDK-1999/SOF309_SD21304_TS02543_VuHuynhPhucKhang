package com.poly.sof309.ui.tests;

import java.math.BigDecimal;
import com.poly.sof309.ui.pages.LoginPage;
import com.poly.sof309.ui.pages.ProductPage;
import com.poly.sof309.ui.pages.ProductPage.ProductRow;
import com.poly.sof309.ui.pages.StorefrontPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfSystemProperty(named = "gearastral.ui.enabled", matches = "true")
class CrudTest extends UiTestSupport {
    private static final String SSD_CATEGORY = "5"; // ID danh mục SSD trong dữ liệu H2 demo
    private static final int INITIAL_STOCK = 4;

    @Test
    @DisplayName("UI-PROD-01: Thêm, sửa và ngừng bán sản phẩm trên GearAstral")
    void staffCanManageProductLifecycle() {
        LoginPage login = new LoginPage(driver, baseUrl);
        login.open();
        login.login(staffUser, staffPassword);
        login.waitForUrl("/dashboard");

        ProductPage products = new ProductPage(driver, baseUrl);
        products.open();
        String name = "SSD QA " + runId();
        products.add(name, SSD_CATEGORY, 1250000, INITIAL_STOCK);

        assertTrue(products.successMessage().contains("Cập nhật thành công"));
        ProductRow created = products.findRow(name);
        assertProduct(created, 1250000, true);

        StorefrontPage store = new StorefrontPage(driver, baseUrl);
        store.openProduct(created.id());
        assertEquals(name, store.productTitle());

        products.open();
        products.cancelEdit(created.id(), name + " chưa lưu");
        products.open();
        assertEquals(created.id(), products.findRow(name).id());

        String updatedName = name + " v2";
        products.edit(created.id(), updatedName, 1230000);
        assertTrue(products.successMessage().contains("Cập nhật thành công"));
        ProductRow updated = products.findRow(updatedName);
        assertEquals(created.id(), updated.id());
        assertProduct(updated, 1230000, true);
        store.openProduct(updated.id());
        assertEquals(updatedName, store.productTitle());

        products.open();
        products.stopSelling(updated.id());
        assertTrue(products.successMessage().contains("Cập nhật thành công"));
        assertFalse(products.findRow(updatedName).active());
        store.openProduct(updated.id());
        assertEquals("404", store.notFoundCode());
    }

    private void assertProduct(ProductRow product, int price, boolean active) {
        assertEquals(0, BigDecimal.valueOf(price).compareTo(new BigDecimal(product.price())),
                "Giá sản phẩm đã lưu");
        assertEquals(INITIAL_STOCK, product.stock(), "Tồn kho");
        assertEquals(active, product.active(), "Trạng thái đang bán");
    }
}
