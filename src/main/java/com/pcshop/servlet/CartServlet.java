package com.pcshop.servlet;

import com.pcshop.dao.OrderDAO;
import com.pcshop.dao.ProductDAO;
import com.pcshop.model.CartItem;
import com.pcshop.model.Order;
import com.pcshop.model.OrderItem;
import com.pcshop.model.Product;
import com.pcshop.model.User;
import com.pcshop.util.InputValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet({"/cart", "/checkout"})
public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int MAX_ITEM_QUANTITY = 99;
    private final ProductDAO productDAO = new ProductDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        attachCart(req);
        String view = "/checkout".equals(req.getServletPath()) ? "/WEB-INF/views/shop/checkout.jsp" : "/WEB-INF/views/shop/cart.jsp";
        req.getRequestDispatcher(view).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = InputValidator.text(req.getParameter("action"), 16);
        if ("/checkout".equals(req.getServletPath()) || "checkout".equals(action)) {
            checkout(req, resp);
            return;
        }

        Map<Integer, CartItem> cart = getCart(req.getSession());
        int productId = InputValidator.positiveInt(req.getParameter("productId"), -1, Integer.MAX_VALUE);
        if ("remove".equals(action)) {
            cart.remove(productId);
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }

        Product product = productId > 0 ? productDAO.getById(productId) : null;
        if (product == null || !product.isActive() || product.getStockQty() <= 0) {
            resp.sendRedirect(req.getContextPath() + "/cart?error=unavailable");
            return;
        }
        int requested = InputValidator.positiveInt(req.getParameter("qty"), 1, MAX_ITEM_QUANTITY);
        int quantity = Math.min(requested, Math.min(product.getStockQty(), MAX_ITEM_QUANTITY));
        CartItem existing = cart.get(productId);
        if ("update".equals(action) && existing != null) {
            existing.setQuantity(quantity);
        } else if (existing == null) {
            cart.put(productId, new CartItem(product, quantity));
        } else {
            existing.setQuantity(Math.min(existing.getQuantity() + quantity, Math.min(product.getStockQty(), MAX_ITEM_QUANTITY)));
        }
        resp.sendRedirect(req.getContextPath() + "/cart");
    }

    private void checkout(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        HttpSession session = req.getSession();
        Map<Integer, CartItem> cart = getCart(session);
        if (cart.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/cart?error=empty");
            return;
        }

        User user = (User) session.getAttribute("loginUser");
        String name = InputValidator.text(firstNonBlank(req.getParameter("customerName"), user == null ? null : user.getFullName()), 100);
        String phone = InputValidator.normalizePhone(firstNonBlank(req.getParameter("customerPhone"), user == null ? null : user.getPhone()));
        String email = InputValidator.text(firstNonBlank(req.getParameter("customerEmail"), user == null ? null : user.getEmail()), 100);
        String address = InputValidator.text(firstNonBlank(req.getParameter("shippingAddr"), user == null ? null : user.getAddress()), 200);
        String notes = InputValidator.multiline(req.getParameter("notes"), 500);
        String validationError = validateCheckout(name, phone, email, address);
        if (validationError != null) {
            forwardCheckoutError(req, resp, validationError, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cart.values()) {
            Product product = productDAO.getById(cartItem.getProduct().getProductId());
            int quantity = cartItem.getQuantity();
            if (product == null || !product.isActive() || quantity < 1 || quantity > product.getStockQty() || quantity > MAX_ITEM_QUANTITY) {
                forwardCheckoutError(req, resp, "Tồn kho vừa thay đổi. Vui lòng kiểm tra lại giỏ hàng.", HttpServletResponse.SC_CONFLICT);
                return;
            }
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            OrderItem item = new OrderItem();
            item.setProductId(product.getProductId());
            item.setQuantity(quantity);
            item.setUnitPrice(product.getPrice());
            item.setSubtotal(subtotal);
            items.add(item);
            total = total.add(subtotal);
        }

        Order order = new Order();
        if (user != null) order.setUserId(user.getUserId());
        order.setStaffId(0);
        order.setCustomerName(name);
        order.setCustomerPhone(phone);
        order.setCustomerEmail(email);
        order.setShippingAddr(address);
        order.setOrderType("product");
        order.setTotalAmount(total);
        order.setDiscount(BigDecimal.ZERO);
        order.setFinalAmount(total);
        order.setPaymentMethod("cod");
        order.setNotes(notes);

        int orderId = orderDAO.createWithItems(order, items);
        if (orderId > 0) {
            cart.clear();
            req.setAttribute("orderId", orderId);
            req.getRequestDispatcher("/WEB-INF/views/shop/checkout-success.jsp").forward(req, resp);
            return;
        }
        String error = orderId == -2 ? "Một số sản phẩm vừa hết hàng. Vui lòng kiểm tra lại giỏ hàng." : "Không thể tạo đơn hàng lúc này. Vui lòng thử lại.";
        forwardCheckoutError(req, resp, error, HttpServletResponse.SC_CONFLICT);
    }

    private String validateCheckout(String name, String phone, String email, String address) {
        if (name.length() < 2) return "Vui lòng nhập họ tên người nhận hợp lệ.";
        if (!InputValidator.phone(phone)) return "Số điện thoại người nhận không hợp lệ.";
        if (!InputValidator.email(email)) return "Email không đúng định dạng.";
        if (address.length() < 5) return "Vui lòng nhập địa chỉ giao hàng đầy đủ.";
        return null;
    }

    private void forwardCheckoutError(HttpServletRequest req, HttpServletResponse resp, String error, int status) throws ServletException, IOException {
        attachCart(req);
        req.setAttribute("error", error);
        resp.setStatus(status);
        req.getRequestDispatcher("/WEB-INF/views/shop/checkout.jsp").forward(req, resp);
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, CartItem> getCart(HttpSession session) {
        Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new LinkedHashMap<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    private void attachCart(HttpServletRequest req) {
        Map<Integer, CartItem> cart = getCart(req.getSession());
        req.setAttribute("cartItems", cart.values());
        req.setAttribute("cartTotal", cartTotal(cart));
    }

    private BigDecimal cartTotal(Map<Integer, CartItem> cart) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart.values()) total = total.add(item.getSubtotal());
        return total;
    }

    private String firstNonBlank(String primary, String fallback) {
        return primary != null && !primary.isBlank() ? primary : fallback;
    }
}
