package com.pcshop.servlet;

import com.pcshop.dao.UserDAO;
import com.pcshop.model.User;
import com.pcshop.util.InputValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/users")
public class UserManageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final UserDAO dao = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("users", dao.getAll());
        req.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        boolean success = switch (InputValidator.text(req.getParameter("action"), 16)) {
            case "toggle" -> toggle(req);
            case "add" -> add(req);
            case "edit" -> edit(req);
            default -> false;
        };
        resp.sendRedirect(req.getContextPath() + "/admin/users?" + (success ? "success=1" : "error=invalid"));
    }

    private boolean toggle(HttpServletRequest req) {
        int userId = InputValidator.positiveInt(req.getParameter("userId"), -1, Integer.MAX_VALUE);
        User current = (User) req.getSession().getAttribute("loginUser");
        return userId > 0 && (current == null || current.getUserId() != userId) && dao.toggleActive(userId);
    }

    private boolean add(HttpServletRequest req) {
        User user = read(req, new User());
        String password = req.getParameter("password");
        if (!valid(user) || !InputValidator.strongPassword(password) || dao.existsUsername(user.getUsername()) || dao.existsEmail(user.getEmail())) return false;
        user.setPassword(password);
        return dao.createManaged(user);
    }

    private boolean edit(HttpServletRequest req) {
        int userId = InputValidator.positiveInt(req.getParameter("userId"), -1, Integer.MAX_VALUE);
        User user = userId > 0 ? dao.getById(userId) : null;
        if (user == null) return false;
        String originalRole = user.getRole();
        read(req, user);
        User current = (User) req.getSession().getAttribute("loginUser");
        if (current != null && current.getUserId() == userId) user.setRole(originalRole);
        if (!valid(user) || !dao.update(user)) return false;
        String password = req.getParameter("password");
        return password == null || password.isBlank() || (InputValidator.strongPassword(password) && dao.resetPassword(userId, password));
    }

    private User read(HttpServletRequest req, User user) {
        if (user.getUserId() == 0) user.setUsername(InputValidator.text(req.getParameter("username"), 32));
        user.setFullName(InputValidator.text(req.getParameter("fullName"), 100));
        user.setEmail(InputValidator.text(req.getParameter("email"), 100));
        user.setPhone(InputValidator.normalizePhone(req.getParameter("phone")));
        user.setAddress(InputValidator.text(req.getParameter("address"), 200));
        user.setRole(InputValidator.role(req.getParameter("role")));
        return user;
    }

    private boolean valid(User user) {
        return user.getFullName() != null && user.getFullName().length() >= 2
                && (user.getUserId() > 0 || InputValidator.username(user.getUsername()))
                && InputValidator.email(user.getEmail())
                && (user.getPhone() == null || user.getPhone().isBlank() || InputValidator.phone(user.getPhone()));
    }
}
