package com.pcshop.servlet;

import com.pcshop.dao.CategoryDAO;
import com.pcshop.dao.ProductDAO;
import com.pcshop.model.Product;
import com.pcshop.util.InputValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@WebServlet({"/shop", "/product"})
public class ShopServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 24;
    private static final BigDecimal MAX_PRICE = new BigDecimal("1000000000");
    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/product".equals(req.getServletPath())) showProduct(req, resp);
        else showCatalog(req, resp);
    }

    private void showProduct(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int id = InputValidator.positiveInt(req.getParameter("id"), -1, Integer.MAX_VALUE);
        Product product = id > 0 ? productDAO.getById(id) : null;
        if (product == null || !product.isActive()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        req.setAttribute("product", product);
        req.setAttribute("pageTitle", product.getProductName() + " | GearAstral");
        String description = InputValidator.text(product.getDescription(), 155);
        req.setAttribute("pageDescription", description.isBlank() ? "Thông số, giá bán và tồn kho " + product.getProductName() + " tại GearAstral." : description);
        req.setAttribute("pageOgType", "product");
        String base = baseUrl(req);
        req.setAttribute("pageOgImage", base + req.getContextPath() + "/product-image?id=" + product.getProductId());
        req.setAttribute("canonicalUrl", base + req.getContextPath() + "/product?id=" + product.getProductId());

        List<Product> related = new ArrayList<>(productDAO.getByCategory(product.getCatId()));
        related.removeIf(item -> item.getProductId() == product.getProductId());
        if (related.size() > 4) related = new ArrayList<>(related.subList(0, 4));
        req.setAttribute("related", related);
        req.getRequestDispatcher("/WEB-INF/views/shop/product-detail.jsp").forward(req, resp);
    }

    private void showCatalog(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String query = InputValidator.text(req.getParameter("q"), 100);
        String brand = InputValidator.text(req.getParameter("brand"), 80);
        String sort = InputValidator.text(req.getParameter("sort"), 16);
        Integer categoryId = nullablePositiveInt(req.getParameter("catId"));
        BigDecimal min = InputValidator.money(req.getParameter("min"), null, MAX_PRICE);
        BigDecimal max = InputValidator.money(req.getParameter("max"), null, MAX_PRICE);
        if (min != null && max != null && min.compareTo(max) > 0) {
            BigDecimal swap = min; min = max; max = swap;
        }

        int requestedPage = InputValidator.positiveInt(req.getParameter("page"), 1, 100_000);
        int total = productDAO.countActive(query, categoryId, brand, min, max);
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        int page = Math.min(requestedPage, totalPages);
        List<Product> products = productDAO.searchActivePage(query, categoryId, brand, min, max, sort, (page - 1) * PAGE_SIZE, PAGE_SIZE);

        req.setAttribute("products", products);
        req.setAttribute("totalProducts", total);
        req.setAttribute("page", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("pageStart", Math.max(1, page - 2));
        req.setAttribute("pageEnd", Math.min(totalPages, page + 2));
        req.setAttribute("categories", categoryDAO.getAll());
        req.setAttribute("brands", productDAO.getActiveBrands());
        req.setAttribute("featured", page == 1 ? productDAO.getLatest(8) : List.of());
        req.getRequestDispatcher("/WEB-INF/views/shop/catalog.jsp").forward(req, resp);
    }

    private Integer nullablePositiveInt(String value) {
        int parsed = InputValidator.positiveInt(value, -1, Integer.MAX_VALUE);
        return parsed > 0 ? parsed : null;
    }

    private String baseUrl(HttpServletRequest req) {
        int port = req.getServerPort();
        boolean defaultPort = (req.isSecure() && port == 443) || (!req.isSecure() && port == 80);
        return req.getScheme() + "://" + req.getServerName() + (defaultPort ? "" : ":" + port);
    }
}
