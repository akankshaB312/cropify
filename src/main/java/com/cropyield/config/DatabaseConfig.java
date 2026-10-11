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
        // When running under the integration‑tests profile we explicitly set the URL via a JVM system property.
        // In this mode we must enforce an in‑memory H2 URL, ignoring any environment variable.
        boolean integration = Boolean.parseBoolean(System.getProperty("integration.tests.enabled", "false"));
        if (integration) {
            String prop = System.getProperty("DB_URL");
            if (prop != null && !prop.isBlank()) {
                if (!prop.startsWith("jdbc:h2:mem:")) {
                    throw new IllegalStateException("Integration tests require an in‑memory H2 URL, but got: " + prop);
                }
                return prop.trim();
            }
            // Default H2 in‑memory URL used by the integration‑tests profile
            return "jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1";
        }
        // Normal mode – first check environment variable, then system property (allowing test overrides), then default.
        String url = System.getenv("DB_URL");
        if (url == null || url.isBlank()) {
            url = System.getProperty("DB_URL");
        }
        return (url == null || url.isBlank()) ? DEFAULT_URL : url.trim();
    }

    /** Returns the database username. Must be set for a production connection. */
    public static String getUser() {
        String user = System.getenv("DB_USER");
        if (user == null || user.isBlank()) {
            // In integration test mode, use H2 default user.
            boolean integration = Boolean.parseBoolean(System.getProperty("integration.tests.enabled", "false"));
            if (integration) {
                return "sa";
            }
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
        // Determine if we are running under the integration‑tests profile.
        boolean integration = Boolean.parseBoolean(System.getProperty("integration.tests.enabled", "false"));
        if (!integration) {
            // Normal mode: just ensure a user is provided (may be any host/database).
            getUser(); // will throw if missing
        } else {
            // Integration mode: we already ensure a safe H2 URL via getUrl() and a default user.
        }
        // Password may be empty; no validation needed
    }
}
