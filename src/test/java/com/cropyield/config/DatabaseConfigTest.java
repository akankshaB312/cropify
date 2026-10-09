package com.cropyield.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple unit tests for {@link DatabaseConfig}. These tests rely on the current environment
 * of the test process. They do not require a live MySQL server.
 */
public class DatabaseConfigTest {

    @Test
    public void testDefaultUrlWhenEnvMissing() {
        // Assuming DB_URL is not set in the test environment; the method should fall back to the default.
        assertEquals("jdbc:mysql://localhost:3306/crop_management", DatabaseConfig.getUrl());
    }

    @Test
    public void testUserRequired() {
        // If DB_USER is not set, getUser() should throw IllegalStateException.
        assertThrows(IllegalStateException.class, DatabaseConfig::getUser);
    }

    @Test
    public void testPasswordDefaultsToEmpty() {
        // If DB_PASSWORD is not set, getPassword() should return an empty string (not null).
        assertEquals("", DatabaseConfig.getPassword());
    }
}
