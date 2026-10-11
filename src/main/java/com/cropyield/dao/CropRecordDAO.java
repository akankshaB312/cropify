package com.cropyield.dao;

import com.cropyield.model.CropRecord;
import com.cropyield.config.DBConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for the {@link CropRecord} table.
 *
 * All operations use {@link DBConnection#getConnection()} and close resources
 * via try‑with‑resources. The SQL statements match the column names in
 * {@code database/schema.sql}.
 */
public final class CropRecordDAO {

    private CropRecordDAO() {
        // utility class – prevent instantiation
    }

    /**
     * Inserts a new {@link CropRecord} and returns the generated primary key.
     * The {@code id} and {@code createdAt} fields of the supplied record are
     * ignored – the database generates them.
     */
    public static long insert(CropRecord record) throws SQLException {
        String sql = "INSERT INTO crop_records " +
                "(farmer_name, crop_id, land_area, rainfall, temperature, soil_type, fertilizer, predicted_yield, created_at) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, record.getFarmerName());
            ps.setInt(2, record.getCropId());
            ps.setBigDecimal(3, record.getLandArea());
            ps.setBigDecimal(4, record.getRainfall());
            ps.setBigDecimal(5, record.getTemperature());
            ps.setString(6, record.getSoilType());
            ps.setBigDecimal(7, record.getFertilizer());
            ps.setBigDecimal(8, record.getPredictedYield());
            // created_at is set to current timestamp by the caller (or can be NOW())
            ps.setTimestamp(9, Timestamp.valueOf(record.getCreatedAt()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Failed to retrieve generated ID for CropRecord insertion.");
            }
        }
    }

    /** Returns all records ordered by newest first (created_at DESC). */
    public static List<CropRecord> listAll() throws SQLException {
        String sql = "SELECT id, farmer_name, crop_id, crop_name, land_area, rainfall, temperature, soil_type, fertilizer, predicted_yield, created_at " +
                "FROM crop_records ORDER BY created_at DESC";
        List<CropRecord> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Finds a record by its primary key. */
    public static Optional<CropRecord> findById(long id) throws SQLException {
        String sql = "SELECT id, farmer_name, crop_id, crop_name, land_area, rainfall, temperature, soil_type, fertilizer, predicted_yield, created_at " +
                "FROM crop_records WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        }
    }

    /** Returns all records for a given crop ID, newest first. */
    public static List<CropRecord> findByCropId(int cropId) throws SQLException {
        String sql = "SELECT id, farmer_name, crop_id, crop_name, land_area, rainfall, temperature, soil_type, fertilizer, predicted_yield, created_at " +
                "FROM crop_records WHERE crop_id = ? ORDER BY created_at DESC";
        List<CropRecord> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cropId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /** Simple farmer‑name search (case‑insensitive, contains). */
    public static List<CropRecord> searchByFarmerName(String term) throws SQLException {
        String sql = "SELECT id, farmer_name, crop_id, crop_name, land_area, rainfall, temperature, soil_type, fertilizer, predicted_yield, created_at " +
                "FROM crop_records WHERE farmer_name LIKE ? ORDER BY created_at DESC";
        List<CropRecord> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + term + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private static CropRecord mapRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String farmerName = rs.getString("farmer_name");
        int cropId = rs.getInt("crop_id");
        // crop_name may be null if not joined; we use empty string if null
        String cropName = rs.getString("crop_name");
        java.math.BigDecimal landArea = rs.getBigDecimal("land_area");
        java.math.BigDecimal rainfall = rs.getBigDecimal("rainfall");
        java.math.BigDecimal temperature = rs.getBigDecimal("temperature");
        String soilType = rs.getString("soil_type");
        java.math.BigDecimal fertilizer = rs.getBigDecimal("fertilizer");
        java.math.BigDecimal predictedYield = rs.getBigDecimal("predicted_yield");
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : null;
        return new CropRecord(id, farmerName, cropId, cropName, landArea, rainfall, temperature, soilType, fertilizer, predictedYield, createdAt);
    }
}
