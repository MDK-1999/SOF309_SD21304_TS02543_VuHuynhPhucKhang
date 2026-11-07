package com.pcshop.servlet;

import com.pcshop.dao.OrderDAO;
import com.pcshop.model.Order;
import com.pcshop.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/customer/orders")
public class CustomerOrderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final OrderDAO orderDAO = new OrderDAO();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("loginUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login?error=session");
            return;
        }

        String id = req.getParameter("id");
        if (id != null && !id.isBlank()) {
            int orderId = parseInt(id, 0);
            Order order = orderDAO.getById(orderId);
            if (order == null || order.getUserId() == null || order.getUserId() != user.getUserId()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            order.setItems(orderDAO.getItems(orderId));
            req.setAttribute("order", order);
            req.getRequestDispatcher("/WEB-INF/views/customer/order-detail.jsp").forward(req, resp);
            return;
        }

        List<Order> orders = orderDAO.getByCustomer(user.getUserId());
        BigDecimal totalSpent = BigDecimal.ZERO;
        int activeOrders = 0;
        for (Order order : orders) {
            if (!"cancelled".equals(order.getStatus())) {
                totalSpent = totalSpent.add(order.getFinalAmount() == null ? BigDecimal.ZERO : order.getFinalAmount());
            }
            if (!"done".equals(order.getStatus()) && !"cancelled".equals(order.getStatus())) activeOrders++;
        }
        req.setAttribute("orders", orders);
        req.setAttribute("orderCount", orders.size());
        req.setAttribute("totalSpent", totalSpent);
        req.setAttribute("activeOrders", activeOrders);
        req.getRequestDispatcher("/WEB-INF/views/customer/orders.jsp").forward(req, resp);
    }

    private int parseInt(String value, int fallback) {
        try { return Integer.parseInt(value); } catch (Exception e) { return fallback; }
    }
}
