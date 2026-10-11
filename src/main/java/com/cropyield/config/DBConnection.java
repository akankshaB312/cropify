package com.cropyield.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides a method to obtain a new JDBC {@link Connection} using the configuration
 * supplied by {@link DatabaseConfig}. The method does **not** cache or close the
 * connection – callers are responsible for managing the lifecycle via try‑with‑resources.
 */
public final class DBConnection {

    static {
        // Load MySQL driver (primary) and H2 driver (for integration tests).
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
            // MySQL driver may not be present in test classpath; ignore.
        }
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Required JDBC driver not found on classpath.", e);
        }
    }

    private DBConnection() {
        // Prevent instantiation
    }

    /**
     * Creates a new {@link Connection} using the environment‑based configuration.
     *
     * @return a live JDBC connection (caller must close it)
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        // Validate required configuration before attempting to connect.
        DatabaseConfig.validate();
        return DriverManager.getConnection(
                DatabaseConfig.getUrl(),
                DatabaseConfig.getUser(),
                DatabaseConfig.getPassword()
        );
    }
}
