package com.pcshop.servlet;

import com.pcshop.dao.UserDAO;
import com.pcshop.model.User;
import com.pcshop.service.RegistrationService;
import com.pcshop.service.RegistrationService.DuplicateAccountException;
import com.pcshop.util.InputValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final RegistrationService registration;

    public RegisterServlet() {
        this(new RegistrationService(new UserDAO()));
    }

    public RegisterServlet(RegistrationService registration) {
        this.registration = registration;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = InputValidator.text(req.getParameter("username"), 33);
        String password = req.getParameter("password");
        String confirm = req.getParameter("confirm");
        String fullName = InputValidator.text(req.getParameter("fullName"), 100);
        String email = InputValidator.text(req.getParameter("email"), 100);
        String phone = InputValidator.normalizePhone(req.getParameter("phone"));
        String address = InputValidator.text(req.getParameter("address"), 200);

        preserve(req, username, fullName, email, phone, address);
        String error = validate(username, password, confirm, fullName, email, phone);
        if (error != null) {
            showError(req, resp, HttpServletResponse.SC_BAD_REQUEST, error);
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setAddress(address);
        try {
            if (registration.register(user)) {
                resp.sendRedirect(req.getContextPath() + "/login?success=registered");
                return;
            }
        } catch (DuplicateAccountException duplicate) {
            showError(req, resp, HttpServletResponse.SC_BAD_REQUEST, duplicate.getMessage());
            return;
        }
        showError(req, resp, HttpServletResponse.SC_CONFLICT, "Không thể tạo tài khoản lúc này. Vui lòng thử lại.");
    }

    private void showError(HttpServletRequest req, HttpServletResponse resp, int status, String message)
            throws ServletException, IOException {
        req.setAttribute("error", message);
        resp.setStatus(status);
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
    }

    private String validate(String username, String password, String confirm, String fullName, String email, String phone) {
        if (fullName.length() < 2) return "Họ tên phải có ít nhất 2 ký tự.";
        if (!InputValidator.username(username)) return "Tên đăng nhập cần 4-32 ký tự, chỉ gồm chữ, số, dấu chấm, gạch dưới hoặc gạch ngang.";
        if (!InputValidator.strongPassword(password)) return "Mật khẩu cần ít nhất 8 ký tự, tối đa 72 byte UTF-8, có chữ hoa, chữ thường và chữ số.";
        if (!password.equals(confirm)) return "Mật khẩu xác nhận không khớp.";
        if (!InputValidator.email(email)) return "Email không đúng định dạng.";
        if (!InputValidator.phone(phone)) return "Số điện thoại Việt Nam không hợp lệ.";
        return null;
    }

    private void preserve(HttpServletRequest req, String username, String fullName, String email, String phone, String address) {
        req.setAttribute("username", username);
        req.setAttribute("fullName", fullName);
        req.setAttribute("email", email);
        req.setAttribute("phone", phone);
        req.setAttribute("address", address);
    }
}
