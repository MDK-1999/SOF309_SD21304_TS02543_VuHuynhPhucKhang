package com.poly.sof309.controllers;

import com.pcshop.dao.UserDAO;
import com.pcshop.model.User;
import com.pcshop.service.RegistrationService;
import com.pcshop.service.RegistrationService.DuplicateAccountException;
import com.pcshop.servlet.LoginServlet;
import com.pcshop.servlet.RegisterServlet;
import com.pcshop.util.LoginAttemptGuard;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuthControllerTest {
    private static final String USERNAME = "gearqa";
    private static final String PASSWORD = "Gear1234";
    private static final String EMAIL = "qa@example.test";
    private static final String CLIENT_IP = "192.0.2.10";
    private static final String CONTEXT = "/gearastral";
    private static final String REGISTER_VIEW = "/WEB-INF/views/register.jsp";

    private UserDAO users;
    private RegistrationService registration;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        users = mock(UserDAO.class);
        registration = new RegistrationService(users);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
        when(request.getRequestDispatcher(REGISTER_VIEW)).thenReturn(dispatcher);
        when(request.getMethod()).thenReturn("POST");
        when(request.getContextPath()).thenReturn(CONTEXT);
    }

    @AfterEach
    void clearLoginAttempts() {
        LoginAttemptGuard.success(CLIENT_IP + ':' + USERNAME);
    }

    @DisplayName("REG-02/03: biên độ dài tên đăng nhập")
    @ParameterizedTest(name = "{0} ký tự, chấp nhận = {1}")
    @CsvSource({"3, false", "4, true", "5, true", "31, true", "32, true", "33, false"})
    void validatesUsernameLength(int length, boolean accepted) throws Exception {
        String username = "a".repeat(length);
        submitRegistration(username, PASSWORD, accepted);

        verifyRegistrationResult(accepted, username, PASSWORD,
                "Tên đăng nhập cần 4-32 ký tự, chỉ gồm chữ, số, dấu chấm, gạch dưới hoặc gạch ngang.");
    }

    @DisplayName("REG-06/07: biên độ dài mật khẩu và giới hạn byte UTF-8")
    @ParameterizedTest(name = "{0} lần ký tự {1}, chấp nhận = {2}")
    @CsvSource({"4, a, false", "5, a, true", "6, a, true", "68, a, true",
            "69, a, true", "70, a, false", "34, é, true", "35, é, false"})
    void validatesPasswordLength(int repetitions, String character, boolean accepted) throws Exception {
        String password = "Ab1" + character.repeat(repetitions);
        submitRegistration(USERNAME, password, accepted);

        verifyRegistrationResult(accepted, USERNAME, password,
                "Mật khẩu cần ít nhất 8 ký tự, tối đa 72 byte UTF-8, có chữ hoa, chữ thường và chữ số.");
    }

    @DisplayName("REG-05/11: trùng tên hoặc email ném ngoại lệ nghiệp vụ")
    @ParameterizedTest(name = "trùng tên = {0}, thông báo = {1}")
    @CsvSource({
            "true, Tên đăng nhập đã được sử dụng.",
            "false, Email đã được sử dụng."
    })
    void rejectsDuplicateAccount(boolean duplicateUsername, String message) throws Exception {
        when(users.existsUsername(USERNAME)).thenReturn(duplicateUsername);
        when(users.existsEmail(EMAIL)).thenReturn(!duplicateUsername);
        User user = new User();
        user.setUsername(USERNAME);
        user.setEmail(EMAIL);

        DuplicateAccountException exception = assertThrows(DuplicateAccountException.class, () -> registration.register(user));
        assertEquals(message, exception.getMessage());

        stubRegistrationForm(USERNAME, PASSWORD);
        new RegisterServlet(registration).service(request, response);
        verifyRegistrationError(HttpServletResponse.SC_BAD_REQUEST, message);
        verify(users, never()).register(any(User.class));
    }

    @Test
    @DisplayName("Đăng ký báo 409 khi DAO không lưu được")
    void reportsConflictWhenSaveFails() throws Exception {
        submitRegistration(USERNAME, PASSWORD, false);

        verify(users).register(any(User.class));
        verifyRegistrationError(HttpServletResponse.SC_CONFLICT,
                "Không thể tạo tài khoản lúc này. Vui lòng thử lại.");
    }

    @Test
    @DisplayName("LOGIN-01/07: đăng nhập đúng đổi mã phiên và chuyển đến dashboard")
    void successfulLoginRotatesSession() throws Exception {
        HttpSession session = stubLoginForm(PASSWORD);
        User user = new User();
        user.setFullName("GearAstral QA");
        when(users.login(USERNAME, PASSWORD)).thenReturn(user);

        new LoginServlet(users).service(request, response);

        verify(request).changeSessionId();
        verify(session).setAttribute("loginUser", user);
        verify(session).setMaxInactiveInterval(3_600);
        verify(response).sendRedirect(CONTEXT + "/dashboard");
    }

    @Test
    @DisplayName("LOGIN-02: thông tin sai đưa về form đăng nhập")
    void invalidCredentialsReturnToLogin() throws Exception {
        HttpSession session = stubLoginForm("Wrong123");

        new LoginServlet(users).service(request, response);

        verify(users).login(USERNAME, "Wrong123");
        verify(session).setAttribute("flashTitle", "ĐĂNG NHẬP THẤT BẠI");
        verify(request, never()).changeSessionId();
        verify(response).sendRedirect(CONTEXT + "/login");
    }

    @Test
    @DisplayName("LOGIN-04: mật khẩu trống bị từ chối trước khi gọi DAO")
    void missingPasswordSkipsLookup() throws Exception {
        HttpSession session = stubLoginForm("");

        new LoginServlet(users).service(request, response);

        verifyNoInteractions(users);
        verify(session).setAttribute("flashTitle", "THÔNG TIN KHÔNG HỢP LỆ");
        verify(response).sendRedirect(CONTEXT + "/login");
    }

    private HttpSession stubLoginForm(String password) {
        HttpSession session = mock(HttpSession.class);
        when(request.getRemoteAddr()).thenReturn(CLIENT_IP);
        when(request.getParameter("username")).thenReturn(USERNAME);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getSession(true)).thenReturn(session);
        return session;
    }

    private void stubRegistrationForm(String username, String password) {
        when(request.getParameter("username")).thenReturn(username);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirm")).thenReturn(password);
        when(request.getParameter("fullName")).thenReturn("GearAstral QA");
        when(request.getParameter("email")).thenReturn(EMAIL);
        when(request.getParameter("phone")).thenReturn("090 000 0000");
    }

    private void submitRegistration(String username, String password, boolean saveSucceeds) throws Exception {
        stubRegistrationForm(username, password);
        when(users.register(any(User.class))).thenReturn(saveSucceeds);
        new RegisterServlet(registration).service(request, response);
    }

    private void verifyRegistrationResult(boolean accepted, String username, String password, String error)
            throws Exception {
        if (!accepted) {
            verifyNoInteractions(users);
            verifyRegistrationError(HttpServletResponse.SC_BAD_REQUEST, error);
            return;
        }

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(users).register(savedUser.capture());
        assertEquals(username, savedUser.getValue().getUsername());
        assertEquals(password, savedUser.getValue().getPassword());
        assertEquals("0900000000", savedUser.getValue().getPhone());
        verify(response).sendRedirect(CONTEXT + "/login?success=registered");
    }

    private void verifyRegistrationError(int status, String message) throws Exception {
        verify(response).setStatus(status);
        verify(request).setAttribute("error", message);
        verify(dispatcher).forward(request, response);
        verify(response, never()).sendRedirect(anyString());
    }
}
