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
    public void checkEnv() {
        // Skip all tests if any required env var is missing
        Assumptions.assumeTrue(System.getenv(REQUIRED_URL) != null && !System.getenv(REQUIRED_URL).isBlank(),
                "Missing DB_URL – integration tests are disabled");
        Assumptions.assumeTrue(System.getenv(REQUIRED_USER) != null && !System.getenv(REQUIRED_USER).isBlank(),
                "Missing DB_USER – integration tests are disabled");
        Assumptions.assumeTrue(System.getenv(REQUIRED_PASS) != null,
                "Missing DB_PASSWORD – integration tests are disabled");
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
