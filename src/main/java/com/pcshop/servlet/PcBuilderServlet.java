package com.pcshop.servlet;

import com.pcshop.dao.CategoryDAO;
import com.pcshop.dao.ProductDAO;
import com.pcshop.model.CartItem;
import com.pcshop.model.Category;
import com.pcshop.model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/pc-builder")
public class PcBuilderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<Category, List<Product>> builderParts = new LinkedHashMap<>();
        List<Category> categories = new ArrayList<>(categoryDAO.getAll());
        categories.sort(Comparator.comparingInt(category -> builderPosition(category.getCatId())));
        for (Category category : categories) {
            List<Product> products = productDAO.getByCategory(category.getCatId());
            if (!products.isEmpty()) builderParts.put(category, products);
        }
        req.setAttribute("builderParts", builderParts);
        req.getRequestDispatcher("/WEB-INF/views/shop/pc-builder.jsp").forward(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        @SuppressWarnings("unchecked")
        Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new LinkedHashMap<>();
            session.setAttribute("cart", cart);
        }
        String[] selectedIds = req.getParameterValues("productId");
        String[] quantities = req.getParameterValues("quantity");
        if (selectedIds != null) {
            for (int index = 0; index < selectedIds.length; index++) {
                int productId = parseInt(selectedIds[index], 0);
                if (productId <= 0) continue;
                Product product = productDAO.getById(productId);
                if (product == null || !product.isActive() || product.getStockQty() <= 0) continue;

                int requestedQuantity = quantities != null && index < quantities.length
                        ? parseInt(quantities[index], 1) : 1;
                requestedQuantity = Math.max(1, Math.min(requestedQuantity, product.getStockQty()));

                CartItem item = cart.get(productId);
                int currentQuantity = item == null ? 0 : item.getQuantity();
                int finalQuantity = Math.min(product.getStockQty(), currentQuantity + requestedQuantity);
                if (item == null) cart.put(productId, new CartItem(product, finalQuantity));
                else item.setQuantity(finalQuantity);
            }
        }
        resp.sendRedirect(req.getContextPath() + "/cart?from=builder");
    }

    private static int builderPosition(int categoryId) {
        int[] order = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        for (int index = 0; index < order.length; index++) {
            if (order[index] == categoryId) return index;
        }
        return order.length + categoryId;
    }

    private int parseInt(String value, int fallback) {
        try { return Integer.parseInt(value); } catch (Exception e) { return fallback; }
    }
}
