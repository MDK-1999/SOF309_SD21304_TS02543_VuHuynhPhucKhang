package com.pcshop.servlet;

import com.pcshop.dao.UserDAO;
import com.pcshop.model.User;
import com.pcshop.util.InputValidator;
import com.pcshop.util.LoginAttemptGuard;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final UserDAO dao = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("loginUser") != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = InputValidator.text(req.getParameter("username"), 33);
        String password = req.getParameter("password");
        String attemptKey = req.getRemoteAddr() + ':' + username.toLowerCase(Locale.ROOT);
        long blockedMillis = LoginAttemptGuard.blockedForMillis(attemptKey);

        if (blockedMillis > 0) {
            long seconds = Math.max(1, blockedMillis / 1_000);
            resp.setHeader("Retry-After", Long.toString(seconds));
            flash(req, "warning", "TẠM KHÓA ĐĂNG NHẬP", "Bạn đã thử quá nhiều lần. Vui lòng đợi khoảng " + Math.max(1, seconds / 60) + " phút.");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (!InputValidator.username(username) || !InputValidator.fitsBcrypt(password) || password.isBlank()) {
            flash(req, "error", "THÔNG TIN KHÔNG HỢP LỆ", "Vui lòng kiểm tra lại tên đăng nhập và mật khẩu.");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = dao.login(username, password);
        if (user == null) {
            LoginAttemptGuard.failure(attemptKey);
            flash(req, "error", "ĐĂNG NHẬP THẤT BẠI", "Tên đăng nhập hoặc mật khẩu không đúng.");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        LoginAttemptGuard.success(attemptKey);
        HttpSession session = req.getSession(true);
        req.changeSessionId();
        session.setAttribute("loginUser", user);
        session.setAttribute("flashLogin", "Chào mừng " + user.getFullName() + "! Đăng nhập thành công.");
        session.setMaxInactiveInterval(3_600);
        resp.sendRedirect(req.getContextPath() + "/dashboard");
    }

    private void flash(HttpServletRequest req, String type, String title, String message) {
        HttpSession session = req.getSession(true);
        session.setAttribute("flashType", type);
        session.setAttribute("flashTitle", title);
        session.setAttribute("flashMsg", message);
    }
}
