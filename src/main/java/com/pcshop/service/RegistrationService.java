package com.pcshop.service;

import com.pcshop.dao.UserDAO;
import com.pcshop.model.User;

public class RegistrationService {
    private final UserDAO users;

    public RegistrationService(UserDAO users) {
        this.users = users;
    }

    public boolean register(User user) throws DuplicateAccountException {
        if (users.existsUsername(user.getUsername())) {
            throw new DuplicateAccountException("Tên đăng nhập đã được sử dụng.");
        }
        String email = user.getEmail();
        if (email != null && !email.isBlank() && users.existsEmail(email)) {
            throw new DuplicateAccountException("Email đã được sử dụng.");
        }
        return users.register(user);
    }

    public static final class DuplicateAccountException extends Exception {
        private static final long serialVersionUID = 1L;

        private DuplicateAccountException(String message) {
            super(message);
        }
    }
}
