package com.pcshop.servlet;

import com.pcshop.dao.OrderDAO;
import com.pcshop.dao.ProductDAO;
import com.pcshop.dao.UserDAO;
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
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@WebServlet("/staff/orders")
public class OrderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Set<String> ORDER_STATUS = Set.of("pending", "confirmed", "processing", "shipping", "done", "cancelled");
    private static final Set<String> PAYMENT_STATUS = Set.of("unpaid", "paid", "refunded");
    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("orders", orderDAO.getAll(100));
        req.setAttribute("products", productDAO.getActive());
        req.setAttribute("customers", userDAO.getByRole("customer"));
        req.getRequestDispatcher("/WEB-INF/views/staff/orders.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = InputValidator.text(req.getParameter("action"), 16);
        if ("create".equals(action)) create(req, resp);
        else if ("status".equals(action)) updateStatus(req, resp);
        else if ("payment".equals(action)) updatePayment(req, resp);
        else resp.sendRedirect(req.getContextPath() + "/staff/orders?error=invalid");
    }

    private void create(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User staff = (User) req.getSession().getAttribute("loginUser");
        String name = InputValidator.text(req.getParameter("customerName"), 100);
        String phone = InputValidator.normalizePhone(req.getParameter("customerPhone"));
        String email = InputValidator.text(req.getParameter("customerEmail"), 100);
        String address = InputValidator.text(req.getParameter("shippingAddr"), 200);
        if (staff == null || name.length() < 2 || (!phone.isBlank() && !InputValidator.phone(phone)) || !InputValidator.email(email)) {
            resp.sendRedirect(req.getContextPath() + "/staff/orders?error=invalid");
            return;
        }

        String[] productIds = req.getParameterValues("productId[]");
        String[] quantities = req.getParameterValues("quantity[]");
        if (productIds == null || quantities == null || productIds.length == 0 || productIds.length != quantities.length) {
            resp.sendRedirect(req.getContextPath() + "/staff/orders?error=items");
            return;
        }

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (int index = 0; index < productIds.length; index++) {
            int productId = InputValidator.positiveInt(productIds[index], -1, Integer.MAX_VALUE);
            int quantity = InputValidator.positiveInt(quantities[index], -1, 99);
            Product product = productId > 0 ? productDAO.getById(productId) : null;
            if (product == null || !product.isActive() || quantity < 1 || quantity > product.getStockQty()) {
                resp.sendRedirect(req.getContextPath() + "/staff/orders?error=stock");
                return;
            }
            OrderItem item = new OrderItem();
            item.setProductId(productId);
            item.setQuantity(quantity);
            item.setUnitPrice(product.getPrice());
            item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
            items.add(item);
            total = total.add(item.getSubtotal());
        }

        Order order = new Order();
        order.setStaffId(staff.getUserId());
        order.setOrderType("product");
        order.setCustomerName(name);
        order.setCustomerPhone(phone);
        order.setCustomerEmail(email);
        order.setShippingAddr(address);
        order.setPaymentMethod("qr".equalsIgnoreCase(req.getParameter("paymentMethod")) ? "qr" : "cod");
        order.setNotes(InputValidator.multiline(req.getParameter("notes"), 500));
        order.setTotalAmount(total);
        order.setDiscount(BigDecimal.ZERO);
        order.setFinalAmount(total);
        int orderId = orderDAO.createWithItems(order, items);
        resp.sendRedirect(req.getContextPath() + "/staff/orders?" + (orderId > 0 ? "success=created" : orderId == -2 ? "error=stock" : "error=create"));
    }

    private void updateStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int orderId = InputValidator.positiveInt(req.getParameter("orderId"), -1, Integer.MAX_VALUE);
        String status = InputValidator.text(req.getParameter("status"), 16);
        boolean updated = orderId > 0 && ORDER_STATUS.contains(status) && orderDAO.updateStatus(orderId, status);
        resp.sendRedirect(req.getContextPath() + "/staff/orders?" + (updated ? "success=updated" : "error=status"));
    }

    private void updatePayment(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int orderId = InputValidator.positiveInt(req.getParameter("orderId"), -1, Integer.MAX_VALUE);
        String paymentStatus = InputValidator.text(req.getParameter("paymentStatus"), 16);
        boolean updated = orderId > 0 && PAYMENT_STATUS.contains(paymentStatus) && orderDAO.updatePayment(orderId, paymentStatus);
        resp.sendRedirect(req.getContextPath() + "/staff/orders?" + (updated ? "success=updated" : "error=payment"));
    }
}
