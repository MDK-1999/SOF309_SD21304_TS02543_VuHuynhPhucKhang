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

@WebServlet("/staff/invoice")
public class StaffInvoiceServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final OrderDAO orderDAO = new OrderDAO();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("loginUser");
        if (user == null || !user.isStaff()) {
            resp.sendRedirect(req.getContextPath() + "/login?error=session");
            return;
        }
        int orderId = parseInt(req.getParameter("id"), 0);
        Order order = orderDAO.getById(orderId);
        if (order == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        order.setItems(orderDAO.getItems(orderId));
        req.setAttribute("order", order);
        req.getRequestDispatcher("/WEB-INF/views/staff/invoice.jsp").forward(req, resp);
    }

    private int parseInt(String value, int fallback) {
        try { return Integer.parseInt(value); } catch (Exception e) { return fallback; }
    }
}
