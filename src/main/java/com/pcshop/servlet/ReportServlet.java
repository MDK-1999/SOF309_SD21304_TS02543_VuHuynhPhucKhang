package com.pcshop.servlet;
import com.pcshop.dao.*;
import jakarta.servlet.ServletException; import jakarta.servlet.annotation.WebServlet; import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/admin/report")
public class ReportServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final OrderDAO   orderDAO   = new OrderDAO();
    private final UserDAO    userDAO    = new UserDAO();
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("todayRevenue",   orderDAO.getTodayRevenue());
        req.setAttribute("monthRevenue",   orderDAO.getMonthRevenue());
        req.setAttribute("totalOrders",    orderDAO.countByStatus(null));
        req.setAttribute("totalCustomers", userDAO.countByRole("customer"));
        req.setAttribute("weeklyRevenue",  orderDAO.getWeeklyRevenue());
        req.setAttribute("recentOrders",   orderDAO.getAll(30));
        req.getRequestDispatcher("/WEB-INF/views/admin/report.jsp").forward(req, resp);
    }
}
