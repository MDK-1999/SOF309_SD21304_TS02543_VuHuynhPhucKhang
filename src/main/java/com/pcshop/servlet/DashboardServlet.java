package com.pcshop.servlet;

import com.pcshop.dao.OrderDAO;
import com.pcshop.dao.ProductDAO;
import com.pcshop.dao.UserDAO;
import com.pcshop.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ProductDAO productDAO = new ProductDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final UserDAO userDAO = new UserDAO();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("loginUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login?error=session");
            return;
        }

        if (user.isCustomer()) {
            resp.sendRedirect(req.getContextPath() + "/customer/orders");
            return;
        }

        req.setAttribute("totalProducts", productDAO.countAll());
        req.setAttribute("lowStock", productDAO.countLowStock(5));
        req.setAttribute("totalOrders", orderDAO.countByStatus(null));
        req.setAttribute("pendingOrders", orderDAO.countByStatus("pending"));
        req.setAttribute("todayRevenue", orderDAO.getTodayRevenue());
        req.setAttribute("monthRevenue", orderDAO.getMonthRevenue());
        req.setAttribute("recentOrders", orderDAO.getAll(10));
        req.setAttribute("weeklyRevenue", orderDAO.getWeeklyRevenue());
        req.setAttribute("totalCustomers", userDAO.countByRole("customer"));
        req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(req, resp);
    }
}
