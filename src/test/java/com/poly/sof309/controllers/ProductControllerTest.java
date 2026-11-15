package com.poly.sof309.controllers;

import com.pcshop.dao.CategoryDAO;
import com.pcshop.dao.ProductDAO;
import com.pcshop.model.Product;
import com.pcshop.servlet.ProductServlet;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ProductControllerTest {
    private static final String CONTEXT = "/gearastral";
    private static final String PRODUCTS_PATH = CONTEXT + "/staff/products";

    private HttpServletRequest request;
    private HttpServletResponse response;
    private ProductDAO products;
    private CategoryDAO categories;
    private ProductServlet controller;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        products = mock(ProductDAO.class);
        categories = mock(CategoryDAO.class);
        controller = new ProductServlet(products, categories);
        when(request.getContextPath()).thenReturn(CONTEXT);
    }

    @DisplayName("PROD-05/07/08/09: các biên form thêm sản phẩm")
    @ParameterizedTest(name = "tên={0}, giá={1}, tồn={2}, chấp nhận={3}")
    @CsvSource({
            "A, 100, 5, false",
            "AB, 100, 5, true",
            "ABC, 100, 5, true",
            "SSD, -1, 5, false",
            "SSD, 0, 5, true",
            "SSD, 1, 5, true",
            "SSD, 999999999, 5, true",
            "SSD, 1000000000, 5, true",
            "SSD, 1000000001, 5, false",
            "SSD, 100, -1, false",
            "SSD, 100, 0, true",
            "SSD, 100, 1, true",
            "SSD, 100, 99999, true",
            "SSD, 100, 100000, true",
            "SSD, 100, 100001, false",
            "SSD, abc, 5, false",
            "SSD, 100, 1.5, false",
            "SSD, 100, '', false"
    })
    void validatesAddForm(String name, String price, String stock, boolean accepted) throws Exception {
        stubProductForm("add", name, price, stock);
        when(products.add(any(Product.class))).thenReturn(true);

        controller.service(request, response);

        if (!accepted) {
            verifyNoInteractions(products);
            verify(response).sendRedirect(PRODUCTS_PATH + "?error=invalid");
            return;
        }

        ArgumentCaptor<Product> savedProduct = ArgumentCaptor.forClass(Product.class);
        verify(products).add(savedProduct.capture());
        Product saved = savedProduct.getValue();
        assertEquals(1, saved.getCatId());
        assertEquals(name, saved.getProductName());
        assertEquals(new BigDecimal(price), saved.getPrice());
        assertEquals(Integer.parseInt(stock), saved.getStockQty());
        verify(response).sendRedirect(PRODUCTS_PATH + "?success=add");
    }

    @Test
    @DisplayName("PROD-02/03: cập nhật trong 2 giây, giữ tồn kho và đổi trạng thái")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void updatesProductAndPreservesStock() throws Exception {
        Product existing = new Product();
        existing.setProductId(25);
        existing.setStockQty(5);
        existing.setActive(true);
        when(products.getById(25)).thenReturn(existing);
        when(products.update(existing)).thenReturn(true);
        stubProductForm("update", "SSD đã sửa", "0", "99999");
        when(request.getParameter("productId")).thenReturn("25");
        when(request.getParameter("isActive")).thenReturn("false");

        controller.service(request, response);

        verify(products).update(existing);
        assertEquals("SSD đã sửa", existing.getProductName());
        assertEquals(BigDecimal.ZERO, existing.getPrice());
        assertEquals(5, existing.getStockQty());
        assertFalse(existing.isActive());
        verify(response).sendRedirect(PRODUCTS_PATH + "?success=update");
    }

    @Test
    @DisplayName("Danh sách sản phẩm chuyển kết quả tìm kiếm và danh mục tới JSP")
    void forwardsSearchResultsToProductView() throws Exception {
        String view = "/WEB-INF/views/staff/products.jsp";
        Product ssd = new Product();
        List<Product> results = List.of(ssd);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("q")).thenReturn("  SSD ");
        when(request.getRequestDispatcher(view)).thenReturn(dispatcher);
        when(products.search("SSD")).thenReturn(results);
        when(categories.getAll()).thenReturn(List.of());

        controller.service(request, response);

        verify(request).setAttribute("products", results);
        verify(request).setAttribute("categories", List.of());
        verify(request).setAttribute("q", "SSD");
        verify(dispatcher).forward(request, response);
    }

    private void stubProductForm(String action, String name, String price, String stock) {
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn(action);
        when(request.getParameter("catId")).thenReturn("1");
        when(request.getParameter("productName")).thenReturn(name);
        when(request.getParameter("price")).thenReturn(price);
        when(request.getParameter("stockQty")).thenReturn(stock);
    }
}
