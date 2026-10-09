package com.cropyield.config;

import java.util.Objects;

/**
 * Holds database configuration values loaded from environment variables.
 * <p>
 * The environment variables used are:
 * <ul>
 *   <li><b>DB_URL</b> – JDBC URL (optional, defaults to {@code jdbc:mysql://localhost:3306/crop_management})</li>
 *   <li><b>DB_USER</b> – Database username (required for production; optional for local dev with empty password)</li>
 *   <li><b>DB_PASSWORD</b> – Database password (required for production; may be empty for local dev)</li>
 * </ul>
 * This class never logs or prints the password.
 */
public final class DatabaseConfig {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/crop_management";

    private DatabaseConfig() {
        // Prevent instantiation
    }

    /** Returns the JDBC URL. If {@code DB_URL} is not set, the default URL is returned. */
    public static String getUrl() {
        String url = System.getenv("DB_URL");
        return (url == null || url.isBlank()) ? DEFAULT_URL : url.trim();
    }

    /** Returns the database username. Must be set for a production connection. */
    public static String getUser() {
        String user = System.getenv("DB_USER");
        if (user == null || user.isBlank()) {
            throw new IllegalStateException("Environment variable DB_USER is not set or is empty.");
        }
        return user.trim();
    }

    /** Returns the database password. May be empty for local development. */
    public static String getPassword() {
        String pwd = System.getenv("DB_PASSWORD");
        // Returning empty string is acceptable; callers should decide whether to allow empty passwords.
        return pwd == null ? "" : pwd;
    }

    /** Validates that required configuration properties are present.
     *  Throws {@link IllegalStateException} if validation fails.
     */
    public static void validate() {
        // URL is always valid (defaults are applied)
        Objects.requireNonNull(getUrl(), "Database URL must not be null");
        // User is required
        getUser(); // will throw if missing
        // Password may be empty; no validation needed
    }
}
