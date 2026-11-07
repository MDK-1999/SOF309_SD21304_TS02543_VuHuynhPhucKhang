package com.pcshop.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.h2.tools.RunScript;

/** Disposable database for the SOF309 local demo. */
final class DemoDatabase {
    private static final String URL = "jdbc:h2:mem:gearastral;MODE=MSSQLServer;IGNORECASE=TRUE;DB_CLOSE_DELAY=-1";
    private static boolean initialized;

    private DemoDatabase() {}

    static Connection connect() throws SQLException {
        initialize();
        return DriverManager.getConnection(URL, "sa", "");
    }

    private static synchronized void initialize() throws SQLException {
        if (initialized) {
            return;
        }
        String password = AppConfig.get("PCSHOP_DEMO_STAFF_PASSWORD", "pcshop.demo.staff.password");
        if (!InputValidator.strongPassword(password)) {
            throw new SQLException("Set PCSHOP_DEMO_STAFF_PASSWORD to a valid GearAstral password.");
        }
        try (Connection connection = DriverManager.getConnection(URL, "sa", "");
                InputStream resource = DemoDatabase.class.getResourceAsStream("/gearastral/demo.sql")) {
            if (resource == null) {
                throw new SQLException("Missing GearAstral demo schema.");
            }
            try (var reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
                RunScript.execute(connection, reader);
            }
            try (var insert = connection.prepareStatement("""
                    INSERT INTO Users(username,password,full_name,role)
                    VALUES ('demostaff', ?, 'Nhân viên kiểm thử', 'staff')
                    """)) {
                insert.setString(1, BCryptUtil.hash(password));
                insert.executeUpdate();
            }
            initialized = true;
        } catch (IOException error) {
            throw new SQLException("Unable to read GearAstral demo schema.", error);
        }
    }
}
