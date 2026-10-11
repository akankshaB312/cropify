package com.cropyield.dao;

import com.cropyield.exception.ValidationException;
import com.cropyield.model.CropRecord;
import com.cropyield.model.CropType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assumptions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the DAO layer.
 *
 * These tests run only when the environment variables {@code DB_URL}, {@code DB_USER}
 * and {@code DB_PASSWORD} point to a dedicated test database.
 * The test database must contain the schema defined in {@code database/schema.sql}
 * (you can execute the script against a fresh MySQL schema before running the tests).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CropDAOIntegrationTest {

    private static final String REQUIRED_URL = "DB_URL";
    private static final String REQUIRED_USER = "DB_USER";
    private static final String REQUIRED_PASS = "DB_PASSWORD";

    @BeforeAll
    public void checkEnv() throws Exception {
        // Ensure integration-test mode is active. The Maven profile sets this flag.
        Assumptions.assumeTrue(Boolean.parseBoolean(System.getProperty("integration.tests.enabled", "false")),
                "Integration tests are disabled – missing integration.tests.enabled flag");
        // Initialise H2 in‑memory schema if not already present.
        try (var conn = com.cropyield.config.DBConnection.getConnection();
             var stmt = conn.createStatement()) {
            // Crop types table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS crop_types (" +
                    "crop_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "crop_name VARCHAR(255) NOT NULL, " +
                    "assumed_yield_per_hectare DECIMAL(10,2), " +
                    "active BOOLEAN NOT NULL)");
            // Seed a default wheat entry if missing
            stmt.executeUpdate("MERGE INTO crop_types (crop_id, crop_name, assumed_yield_per_hectare, active) KEY(crop_id) " +
                    "VALUES (1, 'Wheat', 1.5, TRUE)");
            // Crop records table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS crop_records (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "farmer_name VARCHAR(255), " +
                    "crop_id INT, " +
                    "crop_name VARCHAR(255), " +
                    "land_area DECIMAL(10,2), " +
                    "rainfall DECIMAL(10,2), " +
                    "temperature DECIMAL(10,2), " +
                    "soil_type VARCHAR(255), " +
                    "fertilizer DECIMAL(10,2), " +
                    "predicted_yield DECIMAL(10,2), " +
                    "created_at TIMESTAMP) ");
        }
    }

    @Test
    public void testInsertAndFindCropRecord() throws Exception {
        // Create a simple record for insertion
        CropRecord toInsert = new CropRecord(
                0L,
                "Integration Tester",
                1, // assuming a crop with ID 1 exists in test DB
                "Wheat",
                new BigDecimal("1.5"),
                new BigDecimal("100"),
                new BigDecimal("22"),
                "Loam",
                new BigDecimal("30"),
                new BigDecimal("3000"), // predictedYield (could be calculated beforehand)
                LocalDateTime.now()
        );

        long generatedId = CropRecordDAO.insert(toInsert);
        assertTrue(generatedId > 0, "Generated ID should be positive");

        Optional<CropRecord> fetched = CropRecordDAO.findById(generatedId);
        assertTrue(fetched.isPresent(), "Record must be retrievable by ID");
        CropRecord rec = fetched.get();
        assertEquals(toInsert.getFarmerName(), rec.getFarmerName());
        assertEquals(toInsert.getCropId(), rec.getCropId());
        assertEquals(0, toInsert.getLandArea().compareTo(rec.getLandArea()));
        // Clean up – remove the inserted row
        deleteRecordById(generatedId);
    }

    @Test
    public void testListActiveCropTypes() throws Exception {
        List<CropType> active = CropTypeDAO.listActive();
        assertNotNull(active);
        // At least the demo seeds (Wheat,Rice,Maize) should exist if the test DB is seeded
        assertTrue(active.size() >= 1, "There should be at least one active crop type");
        // Verify that each returned type is marked active
        active.forEach(ct -> assertTrue(ct.isActive()));
    }

    @Test
    public void testFindCropTypeByIdNotFound() throws Exception {
        Optional<CropType> opt = CropTypeDAO.findById(-9999);
        assertTrue(opt.isEmpty(), "Non‑existent ID should return empty Optional");
    }

    @Test
    public void testFindCropTypeByName() throws Exception {
        // Assumes the seed data contains a "Wheat" entry
        Optional<CropType> opt = CropTypeDAO.findByName("Wheat");
        assertTrue(opt.isPresent(), "Wheat should be present in test DB");
        CropType ct = opt.get();
        assertEquals("Wheat", ct.getCropName());
    }

    // Helper to delete a record – used for cleanup in integration tests
    private void deleteRecordById(long id) throws Exception {
        String sql = "DELETE FROM crop_records WHERE id = ?";
        try (var conn = com.cropyield.config.DBConnection.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}
