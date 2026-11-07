package com.pcshop.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    private static final String DEFAULT_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=PCShopDB;encrypt=true;trustServerCertificate=true;loginTimeout=5";

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException error) {
            throw new ExceptionInInitializerError("SQL Server JDBC driver is not available.");
        }
    }

    private DBConnection() {}

    public static boolean isDemo() {
        return "true".equalsIgnoreCase(AppConfig.get("PCSHOP_DEMO", "pcshop.demo", "false"));
    }

    public static Connection getConnection() throws SQLException {
        if (isDemo()) {
            return DemoDatabase.connect();
        }
        String url = AppConfig.get("PCSHOP_DB_URL", "pcshop.db.url");
        if (url.isBlank()) {
            url = AppConfig.get("GEARASTRAL_DB_URL", "gearastral.db.url", DEFAULT_URL);
        }
        String user = AppConfig.get("PCSHOP_DB_USER", "pcshop.db.user", "sa");
        String password = AppConfig.get("PCSHOP_DB_PASSWORD", "pcshop.db.password");
        if (password.isBlank() && !url.toLowerCase().contains("integratedsecurity=true")) {
            throw new SQLException("Missing PCSHOP_DB_PASSWORD. Configure an environment variable or ~/.pcshop/pcshop.properties.");
        }
        return DriverManager.getConnection(url, user, password);
    }
}
