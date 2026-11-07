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

@WebServlet("/staff/products")
public class ProductServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final BigDecimal MAX_PRICE = new BigDecimal("1000000000");
    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String query = InputValidator.text(req.getParameter("q"), 100);
        req.setAttribute("products", query.isBlank() ? productDAO.getAll() : productDAO.search(query));
        req.setAttribute("categories", categoryDAO.getAll());
        req.setAttribute("q", query);
        req.getRequestDispatcher("/WEB-INF/views/staff/products.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = InputValidator.text(req.getParameter("action"), 16);
        boolean success = false;
        if ("add".equals(action)) {
            Product product = readProduct(req, new Product(), true);
            success = valid(product) && productDAO.add(product);
        } else if ("update".equals(action)) {
            int id = InputValidator.positiveInt(req.getParameter("productId"), -1, Integer.MAX_VALUE);
            Product product = id > 0 ? productDAO.getById(id) : null;
            if (product != null) {
                readProduct(req, product, false);
                success = valid(product) && productDAO.update(product);
            }
        }
        resp.sendRedirect(req.getContextPath() + "/staff/products?" + (success ? "success=" + action : "error=invalid"));
    }

    private Product readProduct(HttpServletRequest req, Product product, boolean includeStock) {
        product.setCatId(InputValidator.positiveInt(req.getParameter("catId"), -1, Integer.MAX_VALUE));
        product.setProductName(InputValidator.text(req.getParameter("productName"), 200));
        product.setBrand(InputValidator.text(req.getParameter("brand"), 80));
        product.setModel(InputValidator.text(req.getParameter("model"), 100));
        product.setDescription(InputValidator.multiline(req.getParameter("description"), 2_000));
        product.setSpecs(InputValidator.multiline(req.getParameter("specs"), 1_000));
        product.setPrice(InputValidator.money(req.getParameter("price"), null, MAX_PRICE));
        if (includeStock) product.setStockQty(InputValidator.nonNegativeInt(req.getParameter("stockQty"), -1, 100_000));
        product.setActive(includeStock || "true".equals(req.getParameter("isActive")));
        return product;
    }

    private boolean valid(Product product) {
        return product.getCatId() > 0 && product.getProductName() != null && product.getProductName().length() >= 2
                && product.getPrice() != null && product.getStockQty() >= 0;
    }
}
