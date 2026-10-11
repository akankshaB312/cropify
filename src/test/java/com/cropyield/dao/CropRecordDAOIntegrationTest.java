package com.cropyield.dao;

import com.cropyield.model.CropRecord;
import com.cropyield.config.DatabaseConfig;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link CropRecordDAO} using an in‑memory H2 database.
 * The tests rely on the {@code integration-tests} Maven profile which sets the
 * system property {@code integration.tests.enabled=true} and provides the H2 URL.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CropRecordDAOIntegrationTest {

    @BeforeAll
    void setUpDatabase() throws Exception {
        // Create tables required for the DAO. Using H2 in MySQL compatibility mode.
        try (Connection conn = java.sql.DriverManager.getConnection(
                System.getProperty("DB_URL", "jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1"),
                "sa", "");
             Statement stmt = conn.createStatement()) {
            // Simple schema matching the production tables (columns used by DAO).
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS crop_type (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(100) NOT NULL," +
                    "active BOOLEAN NOT NULL," +
                    "yield_per_hectare DECIMAL(10,2) NOT NULL);"
            );
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS crop_records (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "farmer_name VARCHAR(100) NOT NULL," +
                    "crop_id BIGINT NOT NULL," +
                    "crop_name VARCHAR(100)," +
                    "land_area DECIMAL(10,2) NOT NULL," +
                    "rainfall DECIMAL(10,2)," +
                    "temperature DECIMAL(10,2)," +
                    "soil_type VARCHAR(50)," +
                    "fertilizer DECIMAL(10,2)," +
                    "predicted_yield DECIMAL(12,2) NOT NULL," +
                    "created_at TIMESTAMP NOT NULL," +
                    "FOREIGN KEY (crop_id) REFERENCES crop_type(id)" +
                    ");");
        }
    }

    @AfterAll
    void cleanUpDatabase() throws Exception {
        try (Connection conn = java.sql.DriverManager.getConnection(
                System.getProperty("DB_URL", "jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1"),
                "sa", "");
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS crop_records");
            stmt.executeUpdate("DROP TABLE IF EXISTS crop_type");
        }
    }

    @Test
    void insertAndFindById() throws Exception {
        // Ensure a crop type exists for foreign‑key reference.
        try (Connection conn = java.sql.DriverManager.getConnection(
                System.getProperty("DB_URL", "jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1"),
                "sa", "");
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO crop_type (name, active, yield_per_hectare) VALUES ('Wheat', true, 3000)");
        }
        CropRecord record = new CropRecord(
                0L,
                "John Doe",
                1,
                "Wheat",
                new BigDecimal("2.5"),
                new BigDecimal("100"),
                new BigDecimal("22"),
                "Loam",
                new BigDecimal("50"),
                new BigDecimal("7500"),
                LocalDateTime.now()
        );
        long id = CropRecordDAO.insert(record);
        assertTrue(id > 0, "Generated ID should be positive");
        Optional<CropRecord> opt = CropRecordDAO.findById(id);
        assertTrue(opt.isPresent(), "Record should be found by ID");
        CropRecord fetched = opt.get();
        assertEquals(record.getFarmerName(), fetched.getFarmerName());
        assertEquals(record.getCropId(), fetched.getCropId());
        assertTrue(record.getLandArea().compareTo(fetched.getLandArea()) == 0,
                "Land area values should be numerically equal");
    }

    @Test
    void listAllAndSearch() throws Exception {
        // Insert two records with different farmer names.
        CropRecord r1 = new CropRecord(0L, "Alice", 1, "Wheat", new BigDecimal("1"), new BigDecimal("80"), new BigDecimal("20"), "Sandy", new BigDecimal("30"), new BigDecimal("3000"), LocalDateTime.now());
        CropRecord r2 = new CropRecord(0L, "Bob", 1, "Wheat", new BigDecimal("2"), new BigDecimal("90"), new BigDecimal("21"), "Clay", new BigDecimal("40"), new BigDecimal("6000"), LocalDateTime.now());
        long id1 = CropRecordDAO.insert(r1);
        long id2 = CropRecordDAO.insert(r2);
        List<CropRecord> all = CropRecordDAO.listAll();
        assertTrue(all.size() >= 2, "At least two records should be present");
        List<CropRecord> foundBob = CropRecordDAO.searchByFarmerName("Bob");
        assertEquals(1, foundBob.size(), "Search should return exactly one record for Bob");
        assertEquals("Bob", foundBob.get(0).getFarmerName());
    }
}
