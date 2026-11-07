package com.pcshop.dao;

import com.pcshop.model.User;
import com.pcshop.util.BCryptUtil;
import com.pcshop.util.DBConnection;
import com.pcshop.util.ModernCatalogSeeder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO {
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setAddress(rs.getString("address"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getBoolean("is_active"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) user.setCreatedAt(created.toLocalDateTime());
        return user;
    }

    public User login(String username, String password) {
        ModernCatalogSeeder.ensureSeeded();
        String sql = "SELECT * FROM Users WHERE username=? AND is_active=1";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    User user = mapRow(result);
                    if (BCryptUtil.verify(password, user.getPassword())) return user;
                }
            }
        } catch (SQLException error) {
            LOGGER.log(Level.SEVERE, "Cannot authenticate user", error);
        }
        return null;
    }

    public boolean register(User user) {
        user.setRole("customer");
        return insert(user);
    }

    public boolean createManaged(User user) {
        return insert(user);
    }

    private boolean insert(User user) {
        String sql = "INSERT INTO Users(username,password,full_name,email,phone,address,role) VALUES(?,?,?,?,?,?,?)";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, BCryptUtil.hash(user.getPassword()));
            statement.setString(3, user.getFullName());
            statement.setString(4, emptyToNull(user.getEmail()));
            statement.setString(5, emptyToNull(user.getPhone()));
            statement.setString(6, emptyToNull(user.getAddress()));
            statement.setString(7, user.getRole());
            return statement.executeUpdate() > 0;
        } catch (SQLException error) {
            LOGGER.log(Level.WARNING, "Cannot create user", error);
            return false;
        }
    }

    public boolean existsUsername(String username) {
        return exists("SELECT 1 FROM Users WHERE LOWER(username)=LOWER(?)", username);
    }

    public boolean existsEmail(String email) {
        if (email == null || email.isBlank()) return false;
        return exists("SELECT 1 FROM Users WHERE LOWER(email)=LOWER(?)", email);
    }

    private boolean exists(String sql, String value) {
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException error) {
            LOGGER.log(Level.WARNING, "Cannot check unique user field", error);
            return true;
        }
    }

    public List<User> getAll() {
        ModernCatalogSeeder.ensureSeeded();
        List<User> users = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM Users ORDER BY created_at DESC"); ResultSet result = statement.executeQuery()) {
            while (result.next()) users.add(mapRow(result));
        } catch (SQLException error) {
            LOGGER.log(Level.SEVERE, "Cannot list users", error);
        }
        return users;
    }

    public List<User> getByRole(String role) {
        ModernCatalogSeeder.ensureSeeded();
        List<User> users = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM Users WHERE role=? ORDER BY full_name")) {
            statement.setString(1, role);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) users.add(mapRow(result));
            }
        } catch (SQLException error) {
            LOGGER.log(Level.SEVERE, "Cannot list users by role", error);
        }
        return users;
    }

    public User getById(int id) {
        ModernCatalogSeeder.ensureSeeded();
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM Users WHERE user_id=?")) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return mapRow(result);
            }
        } catch (SQLException error) {
            LOGGER.log(Level.SEVERE, "Cannot find user", error);
        }
        return null;
    }

    public boolean update(User user) {
        String sql = "UPDATE Users SET full_name=?,email=?,phone=?,address=?,role=? WHERE user_id=?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getFullName());
            statement.setString(2, emptyToNull(user.getEmail()));
            statement.setString(3, emptyToNull(user.getPhone()));
            statement.setString(4, emptyToNull(user.getAddress()));
            statement.setString(5, user.getRole());
            statement.setInt(6, user.getUserId());
            return statement.executeUpdate() > 0;
        } catch (SQLException error) {
            LOGGER.log(Level.WARNING, "Cannot update user", error);
            return false;
        }
    }

    public boolean resetPassword(int userId, String newPassword) {
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE Users SET password=? WHERE user_id=?")) {
            statement.setString(1, BCryptUtil.hash(newPassword));
            statement.setInt(2, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException error) {
            LOGGER.log(Level.WARNING, "Cannot reset password", error);
            return false;
        }
    }

    public boolean toggleActive(int userId) {
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE Users SET is_active=1-is_active WHERE user_id=?")) {
            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException error) {
            LOGGER.log(Level.WARNING, "Cannot toggle user state", error);
            return false;
        }
    }

    public int countByRole(String role) {
        ModernCatalogSeeder.ensureSeeded();
        String sql = role == null ? "SELECT COUNT(*) FROM Users" : "SELECT COUNT(*) FROM Users WHERE role=?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            if (role != null) statement.setString(1, role);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        } catch (SQLException error) {
            LOGGER.log(Level.SEVERE, "Cannot count users", error);
            return 0;
        }
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
